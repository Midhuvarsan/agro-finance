package com.agrofinance.repository;
 
import com.agrofinance.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface DocumentRepository extends JpaRepository<Document, Long> {
 
    /** Traverses the farmer association's id — Spring Data parses Farmer_UserId through the relationship. */
    List<Document> findByFarmerUserId(Long userId);
 
}
 












