package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
 
import java.time.LocalDateTime;
 
/**
 * Shared audit columns for every entity that extends this class.
 *
 * @MappedSuperclass: this is NOT a table itself. Its fields are copied
 * ("flattened") into the table of whichever entity extends it — e.g.
 * Farmer extends Auditable means the `farmer` table gets its own
 * created_at / updated_at columns, not a foreign key to some
 * separate "auditable" table.
 *
 * @EntityListeners(AuditingEntityListener.class): hooks this class into
 * Spring Data JPA's auditing lifecycle so the fields below are populated
 * automatically — we never set them by hand in service code.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
 
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
}
 