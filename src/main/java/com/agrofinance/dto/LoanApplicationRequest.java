package com.agrofinance.dto;
 
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
 
import java.math.BigDecimal;
 
public record LoanApplicationRequest(
 
        @NotNull(message = "Loan scheme is required")
        Long loanSchemeId,
 
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least 1")
        BigDecimal amountRequested,
 
        @NotBlank(message = "Purpose is required")
        @Size(max = 500, message = "Purpose must be under 500 characters")
        String purpose
 
) {
}
 


































