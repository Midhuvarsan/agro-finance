package com.agrofinance.service.impl;
 
import com.agrofinance.dto.AuthResponse;
import com.agrofinance.dto.LoginRequest;
import com.agrofinance.dto.RegisterRequest;
import com.agrofinance.entity.Role;
import com.agrofinance.entity.User;
import com.agrofinance.entity.UserStatus;
import com.agrofinance.event.UserRegisteredEvent;
import com.agrofinance.exception.BadRequestException;
import com.agrofinance.exception.DuplicateResourceException;
import com.agrofinance.exception.ForbiddenOperationException;
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
import org.springframework.transaction.annotation.Transactional;
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
 
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
 
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered");
        }
 
        String requestedRole = request.role().toUpperCase();
 
        if (ADMIN_ROLE.equals(requestedRole)) {
            throw new ForbiddenOperationException("Cannot self-register as ADMIN");
        }
 
        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new BadRequestException("Unknown role: " + requestedRole));
 
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(role));
 
        User savedUser = userRepository.save(user);
 
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail()));
 
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtService.generateToken(userDetails);
 
        return buildAuthResponse(savedUser, token);
    }
 
    @Override
    public AuthResponse login(LoginRequest request) {
 
        // NOTE: intentionally left as ResponseStatusException(401), not our
        // custom hierarchy — this is an AUTHENTICATION failure, a distinct
        // concern from business-rule exceptions (Spring Security has its
        // own AuthenticationException family this sits alongside).
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
 
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
 
        return buildAuthResponse(userDetails.getUser(), token);
    }
 
    private AuthResponse buildAuthResponse(User user, String token) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), roleNames);
    }
 
}
