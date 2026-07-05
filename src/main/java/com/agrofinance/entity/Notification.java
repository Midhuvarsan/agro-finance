package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * An in-app notification for a user.
 * Composite index on (user_id, is_read): the "unread notifications for
 * this user" query runs on essentially every page load, so it earns a
 * real index rather than relying on the automatic FK index alone.
 */
@Entity
@Table(
        name = "notification",
        indexes = @Index(name = "idx_notification_user_read", columnList = "user_id, is_read")
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Notification extends Auditable {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    @Column(nullable = false)
    private String title;
 
    @Column(columnDefinition = "TEXT")
    private String message;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
 
    /** Primitive boolean — Lombok generates isRead(), not getRead(). */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
 
}