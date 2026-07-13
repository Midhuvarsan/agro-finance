package com.agrofinance.dto;
 
import java.time.LocalDate;
 
public record FarmerResponse(
        Long userId,
        String email,
        String fullName,
        String aadhaarNumber,
        LocalDate dateOfBirth,
        String address
) {
}