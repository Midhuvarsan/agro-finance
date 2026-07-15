package com.agrofinance.service.impl;
 
import com.agrofinance.dto.AIAnswerResponse;
import com.agrofinance.dto.AIRecommendationResponse;
import com.agrofinance.dto.FarmerReviewResponse;
import com.agrofinance.entity.AIRecommendation;
import com.agrofinance.entity.ChatHistory;
import com.agrofinance.entity.ChatSender;
import com.agrofinance.entity.Loan;
import com.agrofinance.entity.LoanStatus;
import com.agrofinance.exception.AiServiceException;
import com.agrofinance.external.GeminiClient;
import com.agrofinance.repository.AIRecommendationRepository;
import com.agrofinance.repository.ChatHistoryRepository;
import com.agrofinance.repository.LoanRepository;
import com.agrofinance.repository.UserRepository;
import com.agrofinance.service.FarmerService;
import com.agrofinance.service.GeminiAIService;
import com.agrofinance.service.LoanService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
 
import java.math.BigDecimal;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class GeminiAIServiceImpl implements GeminiAIService {
 
    private final GeminiClient geminiClient;
    private final LoanRepository loanRepository;
    private final LoanService loanService;
    private final FarmerService farmerService;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    // ------------------------------------------------------------------
    // 1. Loan Recommendation (the workflow-integrated one)
    // ------------------------------------------------------------------
 
    @Override
    @Transactional
    public AIRecommendationResponse assessLoan(Long loanId) {
 
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
 
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a PENDING loan can be AI-assessed");
        }
 
        // The same composite the human reviewer sees — AI and officer
        // assess from identical data.
        FarmerReviewResponse farmer = farmerService.getReviewDetails(loan.getFarmer().getUserId());
 
        String prompt = """
                You are a credit risk analyst for agricultural loans in India.
                Assess this loan application and respond with ONLY a JSON object,
                no markdown, no explanation outside the JSON, exactly this shape:
                {"riskScore": <number 0-100, higher = riskier>, "recommendation": "<3-5 sentence assessment covering repayment capacity, land adequacy, and a clear suggest-approve/suggest-reject/needs-more-info verdict>"}
 
                APPLICATION:
                Requested amount: %s INR for: %s
                Scheme: %s (interest %s%%, tenure %s months, range %s-%s INR)
 
                FARMER:
                %s, address: %s
                Lands: %s
                Current crops: %s
                Documents on file: %d
                """.formatted(
                loan.getAmountRequested(), loan.getPurpose(),
                loan.getLoanScheme().getName(), loan.getLoanScheme().getInterestRate(),
                loan.getLoanScheme().getTenureMonths(),
                loan.getLoanScheme().getMinAmount(), loan.getLoanScheme().getMaxAmount(),
                farmer.profile().fullName(), farmer.profile().address(),
                summarizeLands(farmer), summarizeCrops(farmer),
                farmer.documents().size()
        );
 
        String aiText = geminiClient.generate(prompt);
        ParsedAssessment parsed = parseAssessment(aiText);
 
        AIRecommendation rec = new AIRecommendation();
        rec.setLoan(loan);
        rec.setRiskScore(parsed.riskScore());
        rec.setRecommendationText(parsed.recommendation());
        rec.setGeminiModelVersion(geminiClient.modelVersion());
        AIRecommendation saved = aiRecommendationRepository.save(rec);
 
        // State machine stays in LoanService — we just invoke the hook.
        loanService.markAiReviewed(loanId);
 
        return toResponse(saved);
    }
 
    @Override
    @Transactional(readOnly = true)
    public AIRecommendationResponse getLatestRecommendation(Long loanId) {
        return aiRecommendationRepository.findTopByLoanIdOrderByCreatedAtDesc(loanId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No AI assessment exists for this loan yet"));
    }
 
    // ------------------------------------------------------------------
    // 2. Explain Loan Decision
    // ------------------------------------------------------------------
 
    @Override
    @Transactional(readOnly = true)
    public AIAnswerResponse explainDecision(Long farmerUserId, Long loanId) {
 
        // Ownership enforced — a farmer can only ask about THEIR loan.
        Loan loan = loanRepository.findByIdAndFarmerUserId(loanId, farmerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
 
        if (loan.getStatus() == LoanStatus.PENDING || loan.getStatus() == LoanStatus.AI_REVIEWED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This loan has not been decided yet");
        }
 
        String prompt = """
                You are a helpful assistant for Indian farmers. Explain this loan
                decision in simple, kind, plain language (no jargon), in under 150
                words. If rejected, briefly suggest what could improve a future
                application. Do not invent details beyond what is given.
 
                Decision: %s
                Requested: %s INR, Approved: %s
                Purpose: %s
                Officer remarks: %s
                """.formatted(
                loan.getStatus(),
                loan.getAmountRequested(),
                loan.getAmountApproved() != null ? loan.getAmountApproved() + " INR" : "n/a",
                loan.getPurpose(),
                loan.getOfficerRemarks() != null ? loan.getOfficerRemarks() : "none recorded"
        );
 
        return new AIAnswerResponse(geminiClient.generate(prompt));
    }
 
    // ------------------------------------------------------------------
    // 3. Government Scheme Suggestions
    // ------------------------------------------------------------------
 
    @Override
    @Transactional(readOnly = true)
    public AIAnswerResponse suggestGovernmentSchemes(Long farmerUserId) {
        FarmerReviewResponse farmer = farmerService.getReviewDetails(farmerUserId);
 
        String prompt = """
                You are an expert on Indian government agricultural schemes
                (central and state). Based on this farmer's profile, suggest the
                3-4 most relevant schemes. For each: name, one-line benefit, and
                where to apply. Keep the whole answer under 250 words. If unsure
                whether a scheme still exists, say so rather than guessing details.
 
                Farmer: address %s
                Lands: %s
                Crops: %s
                """.formatted(
                farmer.profile().address(),
                summarizeLands(farmer), summarizeCrops(farmer)
        );
 
        return new AIAnswerResponse(geminiClient.generate(prompt));
    }
 
    // ------------------------------------------------------------------
    // 4. Crop Recommendation
    // ------------------------------------------------------------------
 
    @Override
    @Transactional(readOnly = true)
    public AIAnswerResponse recommendCrops(Long farmerUserId) {
        FarmerReviewResponse farmer = farmerService.getReviewDetails(farmerUserId);
 
        if (farmer.lands().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Add your land details first so recommendations can use real soil data");
        }
 
        String prompt = """
                You are an agronomist advising an Indian farmer. Based on their
                land and current crops, recommend 2-3 suitable crops to consider
                next season, with brief reasoning per crop (soil fit, rotation
                benefit, typical market demand). Under 200 words, plain language.
 
                Location: %s
                Lands: %s
                Currently growing: %s
                """.formatted(
                farmer.profile().address(),
                summarizeLands(farmer), summarizeCrops(farmer)
        );
 
        return new AIAnswerResponse(geminiClient.generate(prompt));
    }
 
    // ------------------------------------------------------------------
    // 5. Farmer Assistant (with persisted chat history)
    // ------------------------------------------------------------------
 
    @Override
    @Transactional
    public AIAnswerResponse assistant(Long userId, String message) {
 
        // Recent history gives the model conversational context —
        // fetched newest-first, reversed to chronological for the prompt.
        List<ChatHistory> recent = chatHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        StringBuilder history = new StringBuilder();
        for (int i = recent.size() - 1; i >= 0; i--) {
            ChatHistory h = recent.get(i);
            history.append(h.getSender()).append(": ").append(h.getMessage()).append("\n");
        }
 
        String prompt = """
                You are AgroFinance's assistant for Indian farmers. Answer
                questions about farming, loans on this platform (apply, track,
                cancel via the app), and government schemes. Be concise, kind,
                and plain-spoken. If asked something outside these topics,
                gently redirect. Never invent platform features.
 
                Conversation so far:
                %s
                USER: %s
                """.formatted(history.toString(), message);
 
        String answer = geminiClient.generate(prompt);
 
        // Persist BOTH sides — the ChatHistory entity from Phase 2,
        // finally in use.
        var userRef = userRepository.getReferenceById(userId);
 
        ChatHistory userMsg = new ChatHistory();
        userMsg.setUser(userRef);
        userMsg.setSender(ChatSender.USER);
        userMsg.setMessage(message);
        chatHistoryRepository.save(userMsg);
 
        ChatHistory aiMsg = new ChatHistory();
        aiMsg.setUser(userRef);
        aiMsg.setSender(ChatSender.AI);
        aiMsg.setMessage(answer);
        chatHistoryRepository.save(aiMsg);
 
        return new AIAnswerResponse(answer);
    }
 
    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------
 
    private record ParsedAssessment(BigDecimal riskScore, String recommendation) {
    }
 
    /**
     * Parses the structured-output JSON, tolerating the ```json fences
     * models add despite instructions. Clamps score to 0-100 — never
     * trust even "structured" AI output blindly.
     */
    private ParsedAssessment parseAssessment(String aiText) {
        try {
            String cleaned = aiText.strip()
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("```\\s*$", "");
 
            JsonNode node = objectMapper.readTree(cleaned);
            BigDecimal score = new BigDecimal(node.path("riskScore").asText("50"));
            if (score.compareTo(BigDecimal.ZERO) < 0) score = BigDecimal.ZERO;
            if (score.compareTo(BigDecimal.valueOf(100)) > 0) score = BigDecimal.valueOf(100);
 
            String recommendation = node.path("recommendation").asText();
            if (recommendation.isBlank()) {
                throw new AiServiceException("AI assessment was missing a recommendation");
            }
            return new ParsedAssessment(score, recommendation);
 
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Could not parse the AI assessment", e);
        }
    }
 
    private String summarizeLands(FarmerReviewResponse farmer) {
        if (farmer.lands().isEmpty()) return "none recorded";
        return farmer.lands().stream()
                .map(l -> l.areaAcres() + " acres, " + l.soilType() + " soil at " + l.location())
                .reduce((a, b) -> a + "; " + b).orElse("none");
    }
 
    private String summarizeCrops(FarmerReviewResponse farmer) {
        if (farmer.crops().isEmpty()) return "none recorded";
        return farmer.crops().stream()
                .map(c -> c.cropTypeName() + " (" + c.season() + ")")
                .reduce((a, b) -> a + "; " + b).orElse("none");
    }
 
    private AIRecommendationResponse toResponse(AIRecommendation rec) {
        return new AIRecommendationResponse(
                rec.getLoan().getId(),
                rec.getRiskScore(),
                rec.getRecommendationText(),
                rec.getGeminiModelVersion(),
                rec.getCreatedAt()
        );
    }
 
}
 






























