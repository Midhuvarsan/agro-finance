package com.agrofinance.exception;
 
/**
 * Business-level input problem that Bean Validation can't express —
 * an unknown role name, a nonexistent scheme id, cross-field rules
 * like max < min. Mapped to 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}