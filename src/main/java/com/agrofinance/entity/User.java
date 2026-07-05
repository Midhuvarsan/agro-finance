package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
 
/**
 * Authentication and identity ONLY.
 *
 * This table intentionally knows nothing about being a farmer or a bank
 * officer — that is Farmer's and BankOfficer's job (Step 4). Keeping
 * this table narrow is what lets Spring Security depend on User alone,
 * without pulling in domain-specific concerns.
 */
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends Auditable {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String email;
 
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
 
    private String phone;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
 
    /** Null = active record. Non-null = soft-deleted at this timestamp. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
 
    /**
     * User is the OWNING side of this many-to-many: it declares @JoinTable,
     * meaning Hibernate looks here to know how to manage the junction table.
     * No inverse (mappedBy) collection on Role yet — we don't currently
     * need to query "all users with role X" from the Role side.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
 
}