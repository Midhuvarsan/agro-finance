package com.agrofinance.entity;
 
/**
 * Loan application workflow states.
 * NOTE: this enum only defines the possible values — nothing here
 * prevents an invalid jump (e.g. PENDING straight to DISBURSED).
 * Valid transitions will be enforced in the service layer later.
 */
public enum LoanStatus {
    PENDING,
    AI_REVIEWED,
    BANK_APPROVED,
    REJECTED,
    DISBURSED
}