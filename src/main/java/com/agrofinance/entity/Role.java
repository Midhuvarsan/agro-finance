package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Permission-level lookup table — e.g. FARMER, BANK_OFFICER, ADMIN.
 *
 * `name` is a plain String, not a Java enum, on purpose: adding a new
 * role should be a data change (an INSERT), not a code change that
 * requires recompiling and redeploying the application.
 */
@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String name;
 
}