package com.agrofinance.controller;
 
import com.agrofinance.dto.AIAnswerResponse;
import com.agrofinance.dto.AIRecommendationResponse;
import com.agrofinance.dto.ChatRequest;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.GeminiAIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
 
    private final GeminiAIService geminiAIService;
 
    /** Officer triggers the AI assessment on a pending application. */
    @PreAuthorize("hasRole('BANK_OFFICER')")
    @PostMapping("/loans/{loanId}/assess")
    public AIRecommendationResponse assessLoan(@PathVariable Long loanId) {
        return geminiAIService.assessLoan(loanId);
    }
 
    /** Officers/admins read the latest stored assessment for a loan. */
    @PreAuthorize("hasAnyRole('BANK_OFFICER', 'ADMIN')")
    @GetMapping("/loans/{loanId}/recommendation")
    public AIRecommendationResponse latestRecommendation(@PathVariable Long loanId) {
        return geminiAIService.getLatestRecommendation(loanId);
    }
 
    /** Farmer asks for a plain-language explanation of THEIR decided loan. */
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/loans/{loanId}/explain")
    public AIAnswerResponse explainDecision(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long loanId
    ) {
        return geminiAIService.explainDecision(principal.getUser().getId(), loanId);
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/scheme-suggestions")
    public AIAnswerResponse schemeSuggestions(@AuthenticationPrincipal CustomUserDetails principal) {
        return geminiAIService.suggestGovernmentSchemes(principal.getUser().getId());
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/crop-recommendation")
    public AIAnswerResponse cropRecommendation(@AuthenticationPrincipal CustomUserDetails principal) {
        return geminiAIService.recommendCrops(principal.getUser().getId());
    }
 
    /** Any authenticated user can talk to the assistant. */
    @PostMapping("/assistant")
    public AIAnswerResponse assistant(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChatRequest request
    ) {
        return geminiAIService.assistant(principal.getUser().getId(), request.message());
    }
 
}
 






























