package com.agrofinance.dto;
 
import com.agrofinance.validation.ValidAadhaar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
 
import java.time.LocalDate;
 
/**
 * "Complete Profile" request body — creates or updates the Farmer
 * profile for the CURRENTLY AUTHENTICATED user. Notice there is no
 * userId/farmerId field here at all: the target farmer is always
 * derived from the JWT principal, never taken from client input.
 */
public record FarmerRequest(
 
        @NotBlank(message = "Full name is required")
        String fullName,
 
        @NotBlank(message = "Aadhaar number is required")
        @ValidAadhaar
        String aadhaarNumber,
 
        @PastOrPresent(message = "Date of birth cannot be in the future")
        LocalDate dateOfBirth,
 
        String address
 
) {
}
 












