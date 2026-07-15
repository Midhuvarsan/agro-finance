package com.agrofinance.dto;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
public record AIRecommendationResponse(
        Long loanId,
        BigDecimal riskScore,
        String recommendation,
        String modelVersion,
        LocalDateTime assessedAt
) {
}
 






























