package com.agrofinance.exception;
 
/**
 * Thrown when the AI provider is unreachable, times out, or returns an
 * unusable response. Mapped to 503 in GlobalExceptionHandler — the AI
 * being down is a temporary service condition, never a client error
 * and never an opaque 500.
 */
public class AiServiceException extends RuntimeException {
 
    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
 
    public AiServiceException(String message) {
        super(message);
    }
 
}
 






























