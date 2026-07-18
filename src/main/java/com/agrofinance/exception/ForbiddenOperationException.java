package com.agrofinance.exception;
 
/**
 * A business rule (not a role check — Spring Security's
 * AccessDeniedException already covers those) forbids this action:
 * self-registering as ADMIN, an admin suspending their own account.
 * Mapped to 403.
 */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
