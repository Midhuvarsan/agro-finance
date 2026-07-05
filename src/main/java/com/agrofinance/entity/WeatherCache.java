package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDateTime;
 
/**
 * Long-lived historical weather snapshot per location. Standalone —
 * no foreign keys — since it's keyed by location, not by any other
 * entity in this schema.
 *
 * Distinct from Redis (added in a later phase): Redis will cache HOT,
 * short-TTL weather lookups for fast repeated reads. This table is a
 * durable historical ARCHIVE used for yield-prediction trend analysis
 * over months — a job Redis, as an in-memory store, is the wrong tool for.
 */
@Entity
@Table(name = "weather_cache")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WeatherCache {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @Column(name = "location_key", nullable = false)
    private String locationKey;
 
    @Column(name = "weather_json", columnDefinition = "TEXT", nullable = false)
    private String weatherJson;
 
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
 
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
 
}
 
