package com.agrofinance.config;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
 
/**
 * Provides the PasswordEncoder bean used everywhere a password needs to
 * be hashed (registration) or verified (login).
 *
 * BCrypt over plain SHA-256/MD5: BCrypt is deliberately slow (its cost
 * factor controls exactly how slow, tunable as hardware gets faster)
 * and auto-generates a unique salt per password, embedded in the hash
 * itself — defeating both brute-force speed attacks and rainbow tables.
 */
@Configuration
public class PasswordConfig {
 
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Default cost factor (strength) is 10 — a reasonable balance
        // between security and login latency for most applications.
        return new BCryptPasswordEncoder();
    }
 
}
 