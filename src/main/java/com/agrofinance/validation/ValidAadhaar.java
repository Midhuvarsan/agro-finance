package com.agrofinance.validation;
 
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
 
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
/**
 * Custom Bean Validation annotation. @Constraint(validatedBy = ...)
 * links this annotation to the class containing the actual logic —
 * the framework calls AadhaarValidator.isValid() wherever this
 * annotation appears.
 */
@Documented
@Constraint(validatedBy = AadhaarValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAadhaar {
 
    String message() default "Invalid Aadhaar number";
 
    Class<?>[] groups() default {};
 
    Class<? extends Payload>[] payload() default {};
 
}
 












