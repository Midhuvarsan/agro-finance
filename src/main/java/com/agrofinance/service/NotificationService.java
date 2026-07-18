package com.agrofinance.service;
 
import com.agrofinance.dto.NotificationResponse;
import com.agrofinance.dto.PageResponse;
import com.agrofinance.entity.NotificationType;
import org.springframework.data.domain.Pageable;
 
public interface NotificationService {
 
    /** Internal API — called by the event listener and scheduler, never by controllers. */
    void notify(Long userId, String title, String message, NotificationType type);
 
    // ---- User-facing ----
    PageResponse<NotificationResponse> myNotifications(Long userId, Pageable pageable);
 
    long unreadCount(Long userId);
 
    NotificationResponse markRead(Long userId, Long notificationId);
 
    int markAllRead(Long userId);
 
}
 
































