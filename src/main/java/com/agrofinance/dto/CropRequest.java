package com.agrofinance.dto;
 
import jakarta.validation.constraints.NotNull;
 
import java.time.LocalDate;
 
public record CropRequest(
 
        @NotNull(message = "Land id is required")
        Long landId,
 
        @NotNull(message = "Crop type id is required")
        Long cropTypeId,
 
        String season,
 
        LocalDate sowingDate,
 
        LocalDate expectedHarvestDate
 
) {
}
 












