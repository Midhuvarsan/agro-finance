package com.agrofinance.exception;
 
/** The requested resource doesn't exist. Mapped to 404 in GlobalExceptionHandler. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
