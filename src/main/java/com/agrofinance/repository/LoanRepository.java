package com.agrofinance.repository;
 
import com.agrofinance.entity.Loan;
import com.agrofinance.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
import java.util.Optional;
 
public interface LoanRepository extends JpaRepository<Loan, Long> {
 
    /**
     * All fetch joins present because LoanResponse reads farmer name,
     * scheme name, AND officer name — without them, mapping a list of N
     * loans would fire up to 3N extra SELECTs (the N+1 problem, cubed).
     * reviewedBy is LEFT joined: it's null until an officer picks it up.
     */
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer f
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE f.userId = :farmerUserId
            ORDER BY l.createdAt DESC
            """)
    List<Loan> findAllByFarmerUserId(@Param("farmerUserId") Long farmerUserId);
 
    /** Single loan, ownership built into the query — same pattern as LandRepository. */
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer f
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE l.id = :loanId AND f.userId = :farmerUserId
            """)
    Optional<Loan> findByIdAndFarmerUserId(@Param("loanId") Long loanId, @Param("farmerUserId") Long farmerUserId);
 
    /** Officer's review queue — every loan currently in a given status. */
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE l.status = :status
            ORDER BY l.createdAt ASC
            """)
    List<Loan> findAllByStatus(@Param("status") LoanStatus status);
 
    /** Officer view of any single loan (no ownership restriction — role-gated at controller). */
    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.farmer
            JOIN FETCH l.loanScheme
            LEFT JOIN FETCH l.reviewedBy
            WHERE l.id = :loanId
            """)
    Optional<Loan> findByIdWithDetails(@Param("loanId") Long loanId);
 
    /** Guard for "one active application per farmer at a time". */
    boolean existsByFarmerUserIdAndStatusIn(Long farmerUserId, List<LoanStatus> statuses);
 
    /**
     * GROUP BY aggregate — one query for all status counts instead of
     * one COUNT query per status. Returns raw Object[] rows
     * (status, count) which the service reshapes into a Map.
     */
    @Query("SELECT l.status, COUNT(l) FROM Loan l GROUP BY l.status")
    List<Object[]> countGroupedByStatus();
 
    /** COALESCE: SUM over zero rows is NULL in SQL — this makes it 0 instead. */
    @Query("SELECT COALESCE(SUM(l.amountApproved), 0) FROM Loan l WHERE l.status = :status")
    java.math.BigDecimal sumApprovedAmountByStatus(@Param("status") LoanStatus status);
 
    /**
     * Report query: every filter is optional via the (:param IS NULL OR ...)
     * pattern — one query serves all filter combinations instead of a
     * combinatorial explosion of repository methods.
     */
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
                             @Param("from") java.time.LocalDateTime from,
                             @Param("to") java.time.LocalDateTime to);
 
}
 


























