package com.agrofinance.dto;
 
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
 
/**
 * Officer's approval/rejection input.
 *
 * amountApproved is NOT @NotNull here — it's required on approve but
 * irrelevant on reject, and one DTO serves both endpoints. The
 * required-ness is still enforced, just correctly placed: at the
 * business-rule layer in LoanServiceImpl (approve-specific), not here.
 * @DecimalMin still guards against a submitted-but-invalid value.
 *
 * remarks gets a @Size cap now — previously unbounded, meaning a
 * client could push an arbitrarily large string into a TEXT column
 * with no validation-layer pushback.
 */
public record LoanDecisionRequest(
 
        @DecimalMin(value = "1.00", message = "Approved amount must be at least 1")
        java.math.BigDecimal amountApproved,
 
        @Size(max = 1000, message = "Remarks must be under 1000 characters")
        String remarks
 
) {
}
