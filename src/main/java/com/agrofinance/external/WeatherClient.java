package com.agrofinance.external;
 
import com.agrofinance.config.WeatherProperties;
import com.agrofinance.dto.WeatherResponse;
import com.agrofinance.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
 
import java.math.BigDecimal;
 
/**
 * Second external client, same isolation rule as GeminiClient: the only
 * class that knows OpenWeatherMap's wire format. Reuses AiServiceException
 * semantics (external service unavailable -> 503) rather than inventing a
 * parallel exception type for the same failure meaning.
 */
@Component
@Slf4j
public class WeatherClient {
 
    private final WeatherProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    public WeatherClient(WeatherProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
 
    public WeatherResponse currentWeather(String location) {
 
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AiServiceException(
                    "Weather is not configured — set the WEATHER_API_KEY environment variable");
        }
 
        try {
            String raw = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("q", location)
                            .queryParam("units", "metric")
                            .queryParam("appid", properties.getApiKey())
                            .build())
                    .retrieve()
                    .body(String.class);
 
            JsonNode root = objectMapper.readTree(raw);
            return new WeatherResponse(
                    root.path("name").asText(location),
                    BigDecimal.valueOf(root.path("main").path("temp").asDouble()),
                    root.path("main").path("humidity").asInt(),
                    root.path("weather").path(0).path("description").asText("unknown"),
                    BigDecimal.valueOf(root.path("wind").path("speed").asDouble())
            );
 
        } catch (Exception e) {
            log.error("Weather call failed for location '{}'", location, e);
            throw new AiServiceException("Weather service is currently unavailable", e);
        }
    }
 
}
 
































