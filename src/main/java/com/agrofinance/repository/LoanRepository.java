package com.agrofinance.repository;
 
import com.agrofinance.entity.Loan;
import com.agrofinance.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
 
public interface LoanRepository extends JpaRepository<Loan, Long> {
 
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer f
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE f.userId = :farmerUserId
            ORDER BY l.createdAt DESC
            """)
    List<Loan> findAllByFarmerUserId(@Param("farmerUserId") Long farmerUserId);
 
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer f
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE l.id = :loanId AND f.userId = :farmerUserId
            """)
    Optional<Loan> findByIdAndFarmerUserId(@Param("loanId") Long loanId, @Param("farmerUserId") Long farmerUserId);
 
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE l.status = :status
            ORDER BY l.createdAt ASC
            """)
    List<Loan> findAllByStatus(@Param("status") LoanStatus status);
 
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE l.id = :loanId
            """)
    Optional<Loan> findByIdWithDetails(@Param("loanId") Long loanId);
 
    boolean existsByFarmerUserIdAndStatusIn(Long farmerUserId, List<LoanStatus> statuses);
 
    @Query("SELECT l.status, COUNT(l) FROM Loan l GROUP BY l.status")
    List<Object[]> countGroupedByStatus();
 
    @Query("SELECT COALESCE(SUM(l.amountApproved), 0) FROM Loan l WHERE l.status = :status")
    BigDecimal sumApprovedAmountByStatus(@Param("status") LoanStatus status);
 
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE (:status IS NULL OR l.status = :status)
              AND (:from IS NULL OR l.createdAt >= :from)
              AND (:to IS NULL OR l.createdAt <= :to)
            ORDER BY l.createdAt DESC
            """)
    List<Loan> findForReport(@Param("status") LoanStatus status,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);
 
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer
            WHERE l.status IN :statuses AND l.createdAt < :cutoff
            """)
    List<Loan> findStaleInReview(@Param("statuses") List<LoanStatus> statuses,
                                 @Param("cutoff") LocalDateTime cutoff);
 
    // ---- NEW (Phase 11): dashboard aggregates ----
 
    /** Per-farmer status breakdown — same GROUP BY shape as the admin dashboard, scoped to one farmer. */
    @Query("SELECT l.status, COUNT(l) FROM Loan l WHERE l.farmer.userId = :farmerUserId GROUP BY l.status")
    List<Object[]> countGroupedByStatusForFarmer(@Param("farmerUserId") Long farmerUserId);
 
    long countByFarmerUserId(Long farmerUserId);
 
    /** Per-officer status breakdown — only loans THIS officer has personally decided. */
    @Query("SELECT l.status, COUNT(l) FROM Loan l WHERE l.reviewedBy.userId = :officerUserId GROUP BY l.status")
    List<Object[]> countGroupedByStatusForOfficer(@Param("officerUserId") Long officerUserId);
 
    long countByStatus(LoanStatus status);
 
    @Query("SELECT COALESCE(SUM(l.amountApproved), 0) FROM Loan l WHERE l.reviewedBy.userId = :officerUserId")
    BigDecimal sumApprovedAmountByOfficer(@Param("officerUserId") Long officerUserId);
 
}
 


































