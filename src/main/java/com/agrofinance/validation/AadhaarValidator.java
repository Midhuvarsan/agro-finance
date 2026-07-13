package com.agrofinance.validation;
 
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
 
/**
 * Validates Aadhaar format: exactly 12 digits, not starting with 0/1,
 * plus the Verhoeff checksum — the check-digit algorithm Aadhaar
 * actually uses, which a regex fundamentally cannot express (regex
 * checks shape; a checksum requires computation).
 */
public class AadhaarValidator implements ConstraintValidator<ValidAadhaar, String> {
 
    // Verhoeff algorithm tables
    private static final int[][] D = {
            {0,1,2,3,4,5,6,7,8,9},{1,2,3,4,0,6,7,8,9,5},{2,3,4,0,1,7,8,9,5,6},
            {3,4,0,1,2,8,9,5,6,7},{4,0,1,2,3,9,5,6,7,8},{5,9,8,7,6,0,4,3,2,1},
            {6,5,9,8,7,1,0,4,3,2},{7,6,5,9,8,2,1,0,4,3},{8,7,6,5,9,3,2,1,0,4},
            {9,8,7,6,5,4,3,2,1,0}
    };
    private static final int[][] P = {
            {0,1,2,3,4,5,6,7,8,9},{1,5,7,6,2,8,3,0,9,4},{5,8,0,3,7,9,6,1,4,2},
            {8,9,1,6,0,4,3,5,2,7},{9,4,5,3,1,2,6,8,7,0},{4,2,8,6,5,7,3,9,0,1},
            {2,7,9,3,8,0,6,4,1,5},{7,0,4,6,9,1,3,2,5,8}
    };
 
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null/blank handled by @NotBlank separately — a custom validator
        // should validate ONE thing, and treat absence as "not my job".
        if (value == null || value.isBlank()) {
            return true;
        }
 
        if (!value.matches("[2-9][0-9]{11}")) {
            return false;
        }
 
        return verhoeffChecksumValid(value);
    }
 
    private boolean verhoeffChecksumValid(String num) {
        int c = 0;
        int len = num.length();
        for (int i = 0; i < len; i++) {
            int digit = num.charAt(len - 1 - i) - '0';
            c = D[c][P[i % 8][digit]];
        }
        return c == 0;
    }
 
}
 












