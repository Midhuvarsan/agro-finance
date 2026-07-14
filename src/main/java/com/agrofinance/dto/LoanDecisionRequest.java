package com.agrofinance.dto;
 
import jakarta.validation.constraints.DecimalMin;
 
import java.math.BigDecimal;
 
/**
 * Officer's approval input. amountApproved may differ from the
 * requested amount (partial approval) but is required on approve;
 * remarks are optional context either way.
 */
public record LoanDecisionRequest(
 
        @DecimalMin(value = "1.00", message = "Approved amount must be at least 1")
        BigDecimal amountApproved,
 
        String remarks
 
) {
}
 