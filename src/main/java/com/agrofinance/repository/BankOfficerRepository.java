package com.agrofinance.repository;
 
import com.agrofinance.entity.BankOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
 
/**
 * Same shared-PK situation as FarmerRepository: BankOfficer's @Id IS
 * the user id, so findById(Long) covers the officer-by-user lookup.
 */
public interface BankOfficerRepository extends JpaRepository<BankOfficer, Long> {
}
 