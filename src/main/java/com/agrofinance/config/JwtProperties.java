package com.agrofinance.config;
 
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
 
/**
 * Binds the entire "jwt:" block from application.yml into one
 * strongly-typed class, instead of scattering @Value("${jwt.secret}")
 * across multiple files.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
 
    /** Base64-encoded HMAC-SHA signing key. Overridden via JWT_SECRET env var in real deployments. */
    private String secret;
 
    /** Token validity duration, in milliseconds. */
    private long expiration;
 
}
 