package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
 
import java.time.LocalDateTime;
 
/**
 * Immutable audit trail entry. Does NOT extend Auditable — an audit log
 * is never updated after creation, so an updated_at column would be
 * meaningless. Only createdAt is declared, directly.
 *
 * entityName/entityId form a polymorphic reference (not a real FK):
 * this trades referential integrity for the ability to audit changes
 * across every entity type from one single table, rather than needing
 * a dedicated audit table (or a dozen nullable FK columns) per entity.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    /** Nullable — some actions are system-triggered, not user-triggered. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
 
    @Column(nullable = false)
    private String action;
 
    @Column(name = "entity_name")
    private String entityName;
 
    @Column(name = "entity_id")
    private Long entityId;
 
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
}