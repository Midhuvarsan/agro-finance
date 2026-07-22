package com.agrofinance.config;
 
import com.agrofinance.security.CustomUserDetailsService;
import com.agrofinance.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 
/**
 * Replaces Spring Boot's auto-configured "lock everything down with a
 * generated password" default (the one we saw appear back in Step 1)
 * with our own JWT-based, stateless configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // activates @PreAuthorize etc. — used starting Step 11
@RequiredArgsConstructor
public class SecurityConfig {
 
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Safe specifically BECAUSE we use header-based JWTs, not
                // cookies — see class-level explanation for the nuance.
                .csrf(csrf -> csrf.disable())
 
                // No HttpSession anywhere — every request proves its own identity.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
 
                .authenticationProvider(authenticationProvider())
 
                // Our filter runs BEFORE Spring Security's own built-in
                // username/password filter, on every request.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
 
    /**
     * The component that actually checks "does this raw password match
     * the stored hash" during login, using our own UserDetailsService
     * and PasswordEncoder beans from earlier steps.
     *
     * NOTE: Spring Security's recommended construction pattern for this
     * class has changed more than once across recent versions. As of
     * the version this project uses, the correct (non-deprecated) form
     * is: pass UserDetailsService into the constructor, then set the
     * PasswordEncoder via its setter — the reverse of an earlier pattern
     * that took PasswordEncoder in the constructor instead.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
 
    /**
     * Exposed so AuthService (Step 9) can call authenticationManager.authenticate(...)
     * directly during login, rather than needing to know about
     * AuthenticationProvider internals itself.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
 
}