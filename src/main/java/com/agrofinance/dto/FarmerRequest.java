package com.agrofinance.dto;
 
import com.agrofinance.validation.ValidAadhaar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
 
import java.time.LocalDate;
 
public record FarmerRequest(
 
        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must be under 150 characters")
        String fullName,
 
        @NotBlank(message = "Aadhaar number is required")
        @ValidAadhaar
        String aadhaarNumber,
 
        @PastOrPresent(message = "Date of birth cannot be in the future")
        LocalDate dateOfBirth,
 
        @Size(max = 300, message = "Address must be under 300 characters")
        String address
 
) {
}
