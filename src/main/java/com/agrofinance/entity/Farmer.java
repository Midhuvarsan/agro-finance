package com.agrofinance.entity;
 
import jakarta.persistence.CascadeType;
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
 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Farmer-specific profile data. Shares its primary key with User via
 * @MapsId — farmer.user_id is both this table's PK and its FK to
 * user.id. This is composition over inheritance: User handles auth,
 * Farmer handles domain profile, deliberately kept as separate concerns.
 */
@Entity
@Table(name = "farmer")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Farmer extends Auditable {
 
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id")
    private Long userId;
 
    /**
     * @MapsId: this entity's @Id (userId, above) is populated FROM this
     * association, not generated independently. FetchType.LAZY overrides
     * @OneToOne's EAGER default.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
 
    @Column(name = "full_name", nullable = false)
    private String fullName;
 
    @Column(name = "aadhaar_number", nullable = false, unique = true)
    private String aadhaarNumber;
 
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
 
    private String address;
 
    /**
     * Inverse side of Farmer <-> Land. cascade=ALL + orphanRemoval=true
     * because Land is a composition: a land parcel has no independent
     * lifecycle apart from its owning farmer.
     */
    @OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Land> lands = new ArrayList<>();
 
    /**
     * No cascade here, unlike lands above: loans are created directly via
     * LoanRepository with farmer set explicitly, never by adding to this
     * collection — so cascade would add complexity with no real usage.
     */
    @OneToMany(mappedBy = "farmer", fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();
 
    /**
     * No cascade: documents can be re-parented or independently managed
     * (e.g. re-verified) without depending on the Farmer's own lifecycle.
     */
    @OneToMany(mappedBy = "farmer", fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();
 
}