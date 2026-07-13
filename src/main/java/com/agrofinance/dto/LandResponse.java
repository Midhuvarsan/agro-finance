package com.agrofinance.dto;
 
import java.math.BigDecimal;
 
public record LandResponse(
        Long id,
        String surveyNumber,
        BigDecimal areaAcres,
        String location,
        String soilType
) {
}