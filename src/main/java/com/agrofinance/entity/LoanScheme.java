package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 
/**
 * A loan product the bank offers (e.g. "Kisan Credit Card", "Crop Loan
 * Scheme 2026") — admin-managed, reused across many loan applications.
 *
 * NO cascade on the loans collection: deleting or editing a scheme must
 * never affect existing loan applications that reference it. This is
 * aggregation, not composition — contrast with Farmer/Land in Step 5.
 */
@Entity
@Table(name = "loan_scheme")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class LoanScheme extends Auditable {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @Column(nullable = false)
    private String name;
 
    @Column(columnDefinition = "TEXT")
    private String description;
 
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;
 
    @Column(name = "min_amount", precision = 12, scale = 2)
    private BigDecimal minAmount;
 
    @Column(name = "max_amount", precision = 12, scale = 2)
    private BigDecimal maxAmount;
 
    @Column(name = "tenure_months")
    private Integer tenureMonths;
 
    /** No cascade — see class-level note. */
    @OneToMany(mappedBy = "loanScheme", fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();
 
}
 
