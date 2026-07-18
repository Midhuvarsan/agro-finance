package com.agrofinance.controller;
 
import com.agrofinance.dto.NotificationResponse;
import com.agrofinance.dto.PageResponse;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.Map;
 
/** Every endpoint is "my notifications" — identity from JWT, any authenticated role. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
 
    private final NotificationService notificationService;
 
    @GetMapping
    public PageResponse<NotificationResponse> myNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return notificationService.myNotifications(principal.getUser().getId(), pageable);
    }
 
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        return Map.of("unread", notificationService.unreadCount(principal.getUser().getId()));
    }
 
    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id
    ) {
        return notificationService.markRead(principal.getUser().getId(), id);
    }
 
    @PatchMapping("/read-all")
    public Map<String, Integer> markAllRead(@AuthenticationPrincipal CustomUserDetails principal) {
        return Map.of("marked", notificationService.markAllRead(principal.getUser().getId()));
    }
 
}
 
































