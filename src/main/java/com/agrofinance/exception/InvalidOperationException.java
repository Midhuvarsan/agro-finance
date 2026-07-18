package com.agrofinance.exception;
 
/**
 * The request is well-formed but conflicts with the resource's current
 * state or a business rule (illegal loan transition, editing a decided
 * application, cancelling an approved loan, etc.). Mapped to 409.
 */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
