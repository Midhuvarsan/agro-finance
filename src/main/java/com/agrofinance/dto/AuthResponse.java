package com.agrofinance.dto;
 
import java.util.Set;
 
/**
 * Returned after successful register/login. Includes basic identity
 * info alongside the token so the frontend doesn't need to decode the
 * JWT client-side just to know who's logged in and what roles they have.
 */
public record AuthResponse(
 
        String token,
 
        /** Always "Bearer" — tells the client how to present this token
         *  in the Authorization header on subsequent requests. */
        String tokenType,
 
        Long userId,
 
        String email,
 
        Set<String> roles
 
) {
}
 