package com.agrofinance.service;
 
import com.agrofinance.dto.AIAnswerResponse;
import com.agrofinance.dto.AIRecommendationResponse;
 
public interface GeminiAIService {
 
    /** Assess a loan, persist an AIRecommendation, move PENDING -> AI_REVIEWED. */
    AIRecommendationResponse assessLoan(Long loanId);
 
    AIRecommendationResponse getLatestRecommendation(Long loanId);
 
    /** Plain-language explanation of a decided loan, for the owning farmer. */
    AIAnswerResponse explainDecision(Long farmerUserId, Long loanId);
 
    /** Government scheme suggestions from the farmer's real profile data. */
    AIAnswerResponse suggestGovernmentSchemes(Long farmerUserId);
 
    /** Crop recommendations from the farmer's actual land/soil data. */
    AIAnswerResponse recommendCrops(Long farmerUserId);
 
    /** Conversational assistant with persisted chat history. */
    AIAnswerResponse assistant(Long userId, String message);
 
}
 






























