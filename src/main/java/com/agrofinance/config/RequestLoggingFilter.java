package com.agrofinance.config;
 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
 
import java.io.IOException;
import java.util.UUID;
 
/**
 * Assigns each request a short id, stored in SLF4J's MDC so EVERY log
 * line produced while handling that request carries [requestId] — one
 * request becomes greppable end to end across controller/service/error.
 *
 * @Order(HIGHEST_PRECEDENCE) so the id exists before any other filter
 * (including JwtAuthenticationFilter) logs anything.
 *
 * SECURITY: logs method + URI + status + duration only. Never the
 * request body, Authorization header, or query string (which could
 * carry a token) — just the routing shape of the request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
 
    private static final String REQUEST_ID = "requestId";
 
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
 
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID, requestId);
 
        long start = System.currentTimeMillis();
        try {
            log.info("--> {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("<-- {} {} [{}] {}ms",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), duration);
            // MUST clear — threads are pooled and reused; a leftover id
            // would leak onto the next unrelated request on this thread.
            MDC.clear();
        }
    }
 
}
 


































