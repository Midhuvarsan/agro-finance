package com.agrofinance.service.impl;
 
import com.agrofinance.dto.NotificationResponse;
import com.agrofinance.dto.PageResponse;
import com.agrofinance.entity.Notification;
import com.agrofinance.entity.NotificationType;
import com.agrofinance.repository.NotificationRepository;
import com.agrofinance.repository.UserRepository;
import com.agrofinance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
 
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
 
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
 
    @Override
    @Transactional
    public void notify(Long userId, String title, String message, NotificationType type) {
        Notification n = new Notification();
        n.setUser(userRepository.getReferenceById(userId));
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRead(false);
        notificationRepository.save(n);
    }
 
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> myNotifications(Long userId, Pageable pageable) {
        return PageResponse.from(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                        .map(this::toResponse)
        );
    }
 
    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
 
    @Override
    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        n.setRead(true); // dirty checking persists
        return toResponse(n);
    }
 
    @Override
    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId);
    }
 
    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.isRead(), n.getCreatedAt()
        );
    }
 
}
 
































