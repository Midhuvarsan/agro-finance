package com.agrofinance.controller;
 
import com.agrofinance.dto.WeatherResponse;
import com.agrofinance.service.WeatherService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController {
 
    private final WeatherService weatherService;
 
    /** Any authenticated user. e.g. /api/weather?location=Vellore */
    @GetMapping
    public WeatherResponse current(@RequestParam @NotBlank String location) {
        return weatherService.getCurrentWeather(location);
    }
 
}
 
































