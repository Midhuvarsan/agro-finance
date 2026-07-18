package com.agrofinance.repository;
 
import com.agrofinance.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
 
public interface CropRepository extends JpaRepository<Crop, Long> {
 
    /** JOIN FETCH cropType avoids N+1 when listing (CropResponse needs cropType.getName()). */
    @Query("SELECT c FROM Crop c JOIN FETCH c.cropType WHERE c.land.farmer.userId = :farmerUserId")
    List<Crop> findAllByFarmerUserId(@Param("farmerUserId") Long farmerUserId);
 
    /** NEW (Phase 11): derived query traversing land -> farmer -> userId for the dashboard count. */
    long countByLandFarmerUserId(Long farmerUserId);
 
}
 


































