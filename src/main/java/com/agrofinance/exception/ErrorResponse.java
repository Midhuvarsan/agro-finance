package com.agrofinance.exception;
 
import java.time.LocalDateTime;
import java.util.Map;
 
/**
 * The single, consistent error shape every failure in the API produces.
 * fieldErrors is only populated for validation failures; null otherwise
 * (and omitted from JSON — see JacksonInclude note in the handler).
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
}
 












