package com.agrofinance.dto;
 
import java.math.BigDecimal;
 
public record LoanSchemeResponse(
        Long id,
        String name,
        String description,
        BigDecimal interestRate,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Integer tenureMonths
) {
}