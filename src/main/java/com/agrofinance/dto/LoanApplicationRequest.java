package com.agrofinance.dto;
 
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
 
import java.math.BigDecimal;
 
/**
 * Used for both applying (POST) and updating a still-PENDING
 * application (PUT) — same shape, so one DTO. No farmerId (JWT-derived,
 * same IDOR-proof principle as Phase 4) and no status field: a client
 * must NEVER be able to set its own loan status directly.
 */
public record LoanApplicationRequest(
 
        @NotNull(message = "Loan scheme is required")
        Long loanSchemeId,
 
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least 1")
        BigDecimal amountRequested,
 
        @NotBlank(message = "Purpose is required")
        String purpose
 
) {
}
 