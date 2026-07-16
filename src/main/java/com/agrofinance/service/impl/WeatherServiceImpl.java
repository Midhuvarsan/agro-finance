package com.agrofinance.service.impl;
 
import com.agrofinance.constants.CacheNames;
import com.agrofinance.dto.WeatherResponse;
import com.agrofinance.external.WeatherClient;
import com.agrofinance.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
 
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {
 
    private final WeatherClient weatherClient;
 
    /**
     * key = normalized location so "Vellore", "vellore", " VELLORE "
     * share one cache entry instead of burning three API calls.
     * 30-minute TTL is set centrally in CacheConfig.
     */
    @Override
    @Cacheable(cacheNames = CacheNames.WEATHER, key = "#location.trim().toLowerCase()")
    public WeatherResponse getCurrentWeather(String location) {
        return weatherClient.currentWeather(location);
    }
 
}
 
































