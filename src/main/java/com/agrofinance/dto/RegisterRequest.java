package com.agrofinance.dto;
 
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 
/**
 * Registration request body. A Java record, not a Lombok class — DTOs
 * are immutable snapshots, unlike entities which JPA needs to mutate.
 *
 * NOTE: no fullName here — that belongs to the "Complete Profile" step
 * (Phase 4's FarmerRequest), not registration itself. Registration only
 * creates login credentials + role; profile data comes after.
 *
 * NOTE: nothing here stops someone from submitting role="ADMIN".
 * Restricting which roles are allowed to self-register is a business
 * rule, not a shape/validation rule — it belongs in AuthService (Step 9).
 */
public record RegisterRequest(
 
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,
 
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
 
        String phone,
 
        @NotBlank(message = "Role is required")
        String role
 
) {
}
 