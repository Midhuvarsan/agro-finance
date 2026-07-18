package com.agrofinance.exception;
 
/** A resource with this unique identity already exists. Mapped to 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}