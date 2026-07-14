package com.agrofinance.dto;
 
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
 
import java.math.BigDecimal;
 
public record LoanSchemeRequest(
 
        @NotBlank(message = "Scheme name is required")
        String name,
 
        String description,
 
        @NotNull(message = "Interest rate is required")
        @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
        BigDecimal interestRate,
 
        @NotNull(message = "Minimum amount is required")
        @DecimalMin(value = "1.00", message = "Minimum amount must be at least 1")
        BigDecimal minAmount,
 
        @NotNull(message = "Maximum amount is required")
        @DecimalMin(value = "1.00", message = "Maximum amount must be at least 1")
        BigDecimal maxAmount,
 
        @NotNull(message = "Tenure is required")
        @Positive(message = "Tenure must be positive")
        Integer tenureMonths
 
) {
}