package com.agrofinance.dto;
 
import com.agrofinance.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
 
public record UserStatusRequest(
 
        @NotNull(message = "Status is required")
        UserStatus status
 
) {
}
