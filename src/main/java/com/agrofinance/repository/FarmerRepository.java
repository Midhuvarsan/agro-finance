package com.agrofinance.repository;
 
import com.agrofinance.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
 
/**
 * No custom findById needed — Farmer's @Id IS the associated User's id
 * (shared primary key via @MapsId, from Phase 2). JpaRepository's
 * built-in findById(Long) already looks up a farmer by user id for free.
 */
public interface FarmerRepository extends JpaRepository<Farmer, Long> {
 
    boolean existsByAadhaarNumber(String aadhaarNumber);
 
}
 












