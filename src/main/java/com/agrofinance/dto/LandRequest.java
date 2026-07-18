package com.agrofinance.dto;
 
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
 
import java.math.BigDecimal;
 
public record LandRequest(
 
        @NotBlank(message = "Survey number is required")
        @Size(max = 50, message = "Survey number must be under 50 characters")
        String surveyNumber,
 
        @NotNull(message = "Area is required")
        @DecimalMin(value = "0.01", message = "Area must be greater than zero")
        BigDecimal areaAcres,
 
        @Size(max = 200, message = "Location must be under 200 characters")
        String location,
 
        @Size(max = 100, message = "Soil type must be under 100 characters")
        String soilType
 
) {
}
 


































