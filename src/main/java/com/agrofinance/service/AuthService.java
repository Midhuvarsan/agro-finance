package com.agrofinance.service;
 
import com.agrofinance.dto.AuthResponse;
import com.agrofinance.dto.LoginRequest;
import com.agrofinance.dto.RegisterRequest;
 
/**
 * Controllers depend on this interface, never on AuthServiceImpl directly
 * — Dependency Inversion, same principle established for the whole
 * service layer back in Phase 1's package structure discussion.
 */
public interface AuthService {
 
    AuthResponse register(RegisterRequest request);
 
    AuthResponse login(LoginRequest request);
 
}
 