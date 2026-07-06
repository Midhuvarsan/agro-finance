package com.agrofinance.security;
 
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
 
import java.io.IOException;
 
/**
 * Runs on EVERY request, once. If a valid Bearer token is present,
 * marks the request as authenticated. If not, does nothing and lets
 * the request continue — Spring Security's authorization layer
 * (SecurityConfig, Step 8) is what actually rejects unauthenticated
 * requests to protected endpoints, not this filter.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 
    private static final String HEADER_NAME = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
 
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
 
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
 
        String authHeader = request.getHeader(HEADER_NAME);
 
        // No/bad header: just continue. Downstream authorization decides
        // whether this specific endpoint actually required authentication.
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
 
        String token = authHeader.substring(BEARER_PREFIX.length());
 
        try {
            String email = jwtService.extractEmail(token);
 
            // Avoid re-authenticating if something earlier in the chain already did.
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
 
                // Real DB hit here — see JwtService's class notes for why
                // we don't trust the token's embedded roles claim alone.
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
 
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // credentials not needed — already proven via valid token
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
 
                    // THE line that actually marks this request authenticated.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException | org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            // Now logged, not silently swallowed — this is what we need
            // to see to diagnose the current 403 issue.
            log.warn("JWT authentication failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }
 
        filterChain.doFilter(request, response);
    }
 
}
 