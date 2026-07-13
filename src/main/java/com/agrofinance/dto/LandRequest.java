package com.agrofinance.dto;
 
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
 
import java.math.BigDecimal;
 
public record LandRequest(
 
        @NotBlank(message = "Survey number is required")
        String surveyNumber,
 
        @NotNull(message = "Area is required")
        @DecimalMin(value = "0.01", message = "Area must be greater than zero")
        BigDecimal areaAcres,
 
        String location,
 
        String soilType
 
) {
}
 