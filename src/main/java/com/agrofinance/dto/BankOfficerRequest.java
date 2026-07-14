package com.agrofinance.dto;
 
import jakarta.validation.constraints.NotBlank;
 
public record BankOfficerRequest(
 
        @NotBlank(message = "Employee id is required")
        String employeeId,
 
        @NotBlank(message = "Branch name is required")
        String branchName
 
) {
}
 