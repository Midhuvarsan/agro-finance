package com.agrofinance.service.impl;
 
import com.agrofinance.dto.AuthResponse;
import com.agrofinance.dto.LoginRequest;
import com.agrofinance.dto.RegisterRequest;
import com.agrofinance.entity.Role;
import com.agrofinance.entity.User;
import com.agrofinance.entity.UserStatus;
import com.agrofinance.event.UserRegisteredEvent;
import com.agrofinance.repository.RoleRepository;
import com.agrofinance.repository.UserRepository;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.security.JwtService;
import com.agrofinance.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
 
import java.util.Set;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
 
    private static final String ADMIN_ROLE = "ADMIN";
 
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;
 
    /**
     * @Transactional was MISSING here until Phase 10 — an oversight:
     * this method does multiple writes and should always have had it.
     * It also matters now because @TransactionalEventListener silently
     * drops events published outside a transaction.
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public AuthResponse register(RegisterRequest request) {
 
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
 
        String requestedRole = request.role().toUpperCase();
 
        // Business rule enforced HERE, not in the DTO (Step 4) — public
        // self-registration must never be able to grant ADMIN.
        if (ADMIN_ROLE.equals(requestedRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot self-register as ADMIN");
        }
 
        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role: " + requestedRole));
 
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(role));
 
        User savedUser = userRepository.save(user);
 
        // Event fires only after this transaction commits (AFTER_COMMIT
        // listener) — no welcome message for a rolled-back registration.
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail()));
 
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtService.generateToken(userDetails);
 
        return buildAuthResponse(savedUser, token);
    }
 
    @Override
    public AuthResponse login(LoginRequest request) {
 
        Authentication authentication;
        try {
            // Delegates to DaoAuthenticationProvider (Step 8), which uses
            // our CustomUserDetailsService + PasswordEncoder internally.
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            // Deliberately vague — never reveal whether the email or the
            // password was the wrong part, to avoid user enumeration.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
 
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
 
        return buildAuthResponse(userDetails.getUser(), token);
    }
 
    private AuthResponse buildAuthResponse(User user, String token) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName) // clean names here — "FARMER", not the ROLE_-prefixed authority string
                .collect(Collectors.toSet());
 
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), roleNames);
    }
 
}
 
































