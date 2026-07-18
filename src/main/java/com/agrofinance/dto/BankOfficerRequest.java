package com.agrofinance.dto;
 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 
public record BankOfficerRequest(
 
        @NotBlank(message = "Employee id is required")
        @Size(max = 50, message = "Employee id must be under 50 characters")
        String employeeId,
 
        @NotBlank(message = "Branch name is required")
        @Size(max = 150, message = "Branch name must be under 150 characters")
        String branchName
 
) {
}