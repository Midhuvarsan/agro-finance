package com.agrofinance.entity;
 
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
/**
 * A single loan application. The central business entity of the platform.
 * Owning side of three relationships: Farmer, LoanScheme, and (once
 * assigned) BankOfficer.
 */
@Entity
@Table(name = "loan")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Loan extends Auditable {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_scheme_id", nullable = false)
    private LoanScheme loanScheme;
 
    /** Nullable: a freshly submitted loan has no reviewing officer yet. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private BankOfficer reviewedBy;
 
    /** Real currency — precision/scale matter for correctness, not just display. */
    @Column(name = "amount_requested", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountRequested;
 
    @Column(name = "amount_approved", precision = 12, scale = 2)
    private BigDecimal amountApproved;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.PENDING;
 
    @Column(columnDefinition = "TEXT")
    private String purpose;
 
    /**
     * Officer's remarks recorded at approve/reject time. New column —
     * unlike the CANCELLED enum incident, ddl-auto: update CAN handle
     * this (it adds columns; it just never modifies existing ones).
     */
    @Column(name = "officer_remarks", columnDefinition = "TEXT")
    private String officerRemarks;
 
    /** Soft delete, same pattern as User — financial records are never hard-deleted. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
 
    /** No cascade — a document is always anchored to its farmer regardless of loan linkage. */
    @OneToMany(mappedBy = "loan", fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();
 
    /**
     * cascade=ALL + orphanRemoval here IS appropriate, unlike documents
     * above: an AI recommendation has no meaning apart from the specific
     * loan it was generated for — pure composition.
     */
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AIRecommendation> aiRecommendations = new ArrayList<>();
 
}