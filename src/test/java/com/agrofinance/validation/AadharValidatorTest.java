package com.agrofinance.validation;
 
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
 
import static org.assertj.core.api.Assertions.assertThat;
 
/**
 * A ConstraintValidator is a plain class — no Spring context needed,
 * context param is unused by our implementation so null is safe to pass.
 */
@DisplayName("AadhaarValidator — Verhoeff checksum")
class AadhaarValidatorTest {
 
    private final AadhaarValidator validator = new AadhaarValidator();
 
    @Test
    @DisplayName("null/blank is valid — that's @NotBlank's job, not this validator's")
    void nullOrBlank_isValid() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("", null)).isTrue();
    }
 
    @Test
    @DisplayName("a real, checksum-correct Aadhaar (used throughout our manual testing) passes")
    void validAadhaar_passesChecksum() {
        assertThat(validator.isValid("234123412346", null)).isTrue();
    }
 
    @ParameterizedTest(name = "[{index}] {0} starts with 0/1 or is the wrong length -> invalid")
    @ValueSource(strings = {
            "123456789012", // starts with 1 — invalid per Aadhaar rules
            "012345678901", // starts with 0
            "23412341234",  // 11 digits, too short
            "2341234123467" // 13 digits, too long
    })
    @DisplayName("fails the shape check before checksum is even computed")
    void badShape_isInvalid(String value) {
        assertThat(validator.isValid(value, null)).isFalse();
    }
 
    @Test
    @DisplayName("right shape, WRONG checksum digit — proves it's a real Verhoeff check, not just a regex")
    void rightShapeWrongChecksum_isInvalid() {
        // Same as the valid number above, last digit changed — shape passes, checksum must fail.
        assertThat(validator.isValid("234123412340", null)).isFalse();
    }
 
}
 


































