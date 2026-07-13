package com.agrofinance.dto;
 
import java.time.LocalDate;
 
public record CropResponse(
        Long id,
        Long landId,
        String cropTypeName,
        String season,
        LocalDate sowingDate,
        LocalDate expectedHarvestDate
) {
}
 












