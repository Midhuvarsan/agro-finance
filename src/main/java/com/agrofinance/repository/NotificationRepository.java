package com.agrofinance.repository;
 
import com.agrofinance.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.Optional;
 
public interface NotificationRepository extends JpaRepository<Notification, Long> {
 
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
 
    /** Served by the (user_id, is_read) composite index from Phase 2. */
    long countByUserIdAndReadFalse(Long userId);
 
    /** Ownership in the query — same pattern as loans/lands. */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
 
    /**
     * @Modifying: this @Query WRITES (bulk UPDATE) instead of reading —
     * one SQL statement marks everything read, versus loading N entities
     * and dirty-checking each. First bulk-update query in the project.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllRead(@Param("userId") Long userId);
 
}
 
































