package com.agrofinance.controller;
 
import com.agrofinance.dto.AuthResponse;
import com.agrofinance.dto.LoginRequest;
import com.agrofinance.dto.RegisterRequest;
import com.agrofinance.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
/**
 * @RestController = @Controller + @ResponseBody: every method's return
 * value is serialized directly to the HTTP response body (as JSON, via
 * Jackson) instead of being resolved as a view name.
 *
 * Deliberately thin, per the Phase 1 architecture: this class only
 * translates HTTP <-> service calls. No business logic lives here.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
 
    private final AuthService authService;
 
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println(request);
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
 
}