package com.agrofinance.repository;
 
import com.agrofinance.entity.Land;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
 
public interface LandRepository extends JpaRepository<Land, Long> {
 
    List<Land> findByFarmerUserId(Long userId);
 
    /** Fetches a land parcel ONLY if it belongs to the given farmer — ownership check built into the query. */
    Optional<Land> findByIdAndFarmerUserId(Long id, Long farmerUserId);
 
    /** NEW (Phase 11): total acreage for the farmer dashboard. COALESCE avoids NULL when a farmer has no land yet. */
    @Query("SELECT COALESCE(SUM(l.areaAcres), 0) FROM Land l WHERE l.farmer.userId = :farmerUserId")
    BigDecimal sumAreaByFarmerUserId(@Param("farmerUserId") Long farmerUserId);
 
}
 


































