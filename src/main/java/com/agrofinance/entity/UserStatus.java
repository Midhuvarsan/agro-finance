package com.agrofinance.entity;
 
/**
 * Fixed, code-controlled set of account states.
 * Kept as a Java enum (not a lookup table like Role) because these
 * values are core application logic, not data an admin should be
 * adding to at runtime.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
