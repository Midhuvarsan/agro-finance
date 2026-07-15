package com.agrofinance.repository;
 
import com.agrofinance.entity.AIRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {
 
    Optional<AIRecommendation> findTopByLoanIdOrderByCreatedAtDesc(Long loanId);
 
}
 






























