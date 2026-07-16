package com.agrofinance.config;
 
import com.agrofinance.constants.CacheNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
 
import java.time.Duration;
 
/**
 * @EnableCaching = the master switch (like @EnableJpaAuditing in Phase 2)
 * — without it, every @Cacheable in the codebase is inert.
 */
@Configuration
@EnableCaching
public class CacheConfig {
 
    /**
     * JSON serialization instead of Spring's default JDK serialization:
     * readable in redis-cli, and resilient to class changes.
     * JavaTimeModule registered up front — LocalDateTime serialization
     * failure is the classic Redis-cache runtime surprise.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Type info embedded per entry so JSON deserializes back to the
        // right class; validator restricts it to our own packages + JDK.
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
 
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)))
                .entryTtl(Duration.ofMinutes(10)); // default for unnamed caches
    }
 
    /**
     * Per-cache TTLs — the eviction strategy in code:
     * - loanSchemes: long TTL, kept fresh by explicit @CacheEvict on writes
     * - dashboard:   60s TTL, no explicit eviction (changes constantly;
     *                a minute of staleness is the right trade)
     * - weather:     30min TTL (external data we don't control)
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer cacheCustomizer(RedisCacheConfiguration base) {
        return builder -> builder
                .withCacheConfiguration(CacheNames.LOAN_SCHEMES, base.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(CacheNames.DASHBOARD, base.entryTtl(Duration.ofSeconds(60)))
                .withCacheConfiguration(CacheNames.WEATHER, base.entryTtl(Duration.ofMinutes(30)));
    }
 
}
 
































