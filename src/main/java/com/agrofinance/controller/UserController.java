package com.agrofinance.controller;
 
import com.agrofinance.dto.UserResponse;
import com.agrofinance.entity.Role;
import com.agrofinance.security.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.stream.Collectors;
 
@RestController
@RequestMapping("/api/users")
public class UserController {
 
    /**
     * Any authenticated user, regardless of role — this is our proof
     * that JwtAuthenticationFilter + SecurityConfig are correctly
     * wired together end to end.
     */
    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        var roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getEmail(), roleNames);
    }
 
    /**
     * TEMPORARY verification endpoint — exists only to prove role-based
     * authorization actually blocks non-farmers, not just unauthenticated
     * requests. Will be replaced by a real Farmer feature controller in
     * a later phase.
     *
     * hasRole('FARMER') checks for authority "ROLE_FARMER" — matches
     * what CustomUserDetails.getAuthorities() produces (see Step 5).
     */
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/farmer-check")
    public String farmerOnlyCheck() {
        return "If you can read this, you're authenticated AND have the FARMER role.";
    }
 
}