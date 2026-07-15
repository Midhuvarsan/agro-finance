package com.agrofinance.dto;
 
import com.agrofinance.entity.UserStatus;
 
import java.time.LocalDateTime;
import java.util.Set;
 
public record AdminUserResponse(
        Long id,
        String email,
        String phone,
        UserStatus status,
        Set<String> roles,
        LocalDateTime createdAt
) {
}