package com.agrofinance.repository;
 
import com.agrofinance.entity.Land;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface LandRepository extends JpaRepository<Land, Long> {
 
    List<Land> findByFarmerUserId(Long userId);
 
    /**
     * Fetches a land parcel ONLY if it belongs to the given farmer —
     * ownership check built into the query itself, used by addCrop to
     * stop a farmer attaching crops to someone else's land.
     */
    Optional<Land> findByIdAndFarmerUserId(Long id, Long farmerUserId);
 
}
 












