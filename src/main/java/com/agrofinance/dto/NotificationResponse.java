package com.agrofinance.dto;
 
import com.agrofinance.entity.NotificationType;
 
import java.time.LocalDateTime;
 
public record NotificationResponse(
        Long id,
        String title,
        String message,
        NotificationType type,
        boolean read,
        LocalDateTime createdAt
) {
}
 
































