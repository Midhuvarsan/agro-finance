package com.agrofinance.repository;
 
import com.agrofinance.entity.LoanScheme;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface LoanSchemeRepository extends JpaRepository<LoanScheme, Long> {
 
    boolean existsByName(String name);
 
}
