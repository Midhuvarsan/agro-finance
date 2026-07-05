package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.util.ArrayList;
import java.util.List;
 
/**
 * Bank-officer-specific profile data. Same shared-primary-key pattern
 * as Farmer — see Farmer.java for the full explanation of @MapsId.
 */
@Entity
@Table(name = "bank_officer")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class BankOfficer extends Auditable {
 
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id")
    private Long userId;
 
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
 
    @Column(name = "employee_id", nullable = false, unique = true)
    private String employeeId;
 
    @Column(name = "branch_name", nullable = false)
    private String branchName;
 
    @OneToMany(mappedBy = "reviewedBy", fetch = FetchType.LAZY)
    private List<Loan> reviewedLoans = new ArrayList<>();
 
}