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
 
}