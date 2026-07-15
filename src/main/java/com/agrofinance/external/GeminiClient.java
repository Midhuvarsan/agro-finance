package com.agrofinance.external;
 
import com.agrofinance.config.GeminiProperties;
import com.agrofinance.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
 
import java.util.List;
import java.util.Map;
 
/**
 * The ONLY class that knows Gemini's wire format. Everything above this
 * sends a prompt string and receives an answer string — swap the AI
 * provider and only this file changes (the Phase 1 external/ rule).
 *
 * Uses RestClient (Spring 6.1+) — the designated successor to
 * RestTemplate for synchronous HTTP; no extra dependency needed.
 */
@Component
@Slf4j
public class GeminiClient {
 
    private final GeminiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    public GeminiClient(GeminiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
 
    /**
     * Sends one prompt, returns the model's text answer.
     * Request shape:  {"contents":[{"parts":[{"text": prompt}]}]}
     * Response shape: candidates[0].content.parts[0].text
     */
    public String generate(String prompt) {
 
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AiServiceException(
                    "AI is not configured — set the GEMINI_API_KEY environment variable");
        }
 
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );
 
        try {
            String rawResponse = restClient.post()
                    .uri("/models/{model}:generateContent", properties.getModel())
                    .header("x-goog-api-key", properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
 
            return extractText(rawResponse);
 
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            // Key detail: log the full cause server-side, but the message
            // that reaches clients (via the 503 handler) stays generic —
            // provider error bodies can leak key hints and internals.
            log.error("Gemini call failed", e);
            throw new AiServiceException("AI service is currently unavailable", e);
        }
    }
 
    /** Defensive parse — AI APIs change/omit fields more than most. */
    private String extractText(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode textNode = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                log.error("Unexpected Gemini response shape: {}", rawResponse);
                throw new AiServiceException("AI returned an unusable response");
            }
            return textNode.asText();
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse AI response", e);
        }
    }
 
    public String modelVersion() {
        return properties.getModel();
    }
 
}
 






























