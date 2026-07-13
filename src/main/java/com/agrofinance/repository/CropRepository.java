package com.agrofinance.repository;
 
import com.agrofinance.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
 
public interface CropRepository extends JpaRepository<Crop, Long> {
 
    /**
     * JOIN FETCH cropType: CropResponse needs cropType.getName(), and
     * without the fetch join this listing would fire one extra SELECT
     * per crop — the classic N+1 problem, avoided the same way we fixed
     * User.roles in Phase 3.
     */
    @Query("SELECT c FROM Crop c JOIN FETCH c.cropType WHERE c.land.farmer.userId = :farmerUserId")
    List<Crop> findAllByFarmerUserId(@Param("farmerUserId") Long farmerUserId);
 
}
 












