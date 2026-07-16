package com.agrofinance.dto;
 
import java.math.BigDecimal;
 
/** Deliberately time-free and simple — this exact object gets serialized into Redis. */
public record WeatherResponse(
        String location,
        BigDecimal temperatureCelsius,
        int humidityPercent,
        String description,
        BigDecimal windSpeedMps
) {
}
 
































