package com.agrofinance.service;
 
import com.agrofinance.dto.CropRequest;
import com.agrofinance.dto.CropResponse;
import com.agrofinance.dto.LandRequest;
import com.agrofinance.dto.LandResponse;
 
import java.util.List;
 
/**
 * Land + Crop operations in one service: they form one cohesive
 * "farm data" concern (a crop can't exist without a land), and neither
 * is big enough alone to justify a separate service yet.
 */
public interface FarmService {
 
    LandResponse addLand(Long farmerUserId, LandRequest request);
 
    List<LandResponse> listMyLands(Long farmerUserId);
 
    CropResponse addCrop(Long farmerUserId, CropRequest request);
 
    List<CropResponse> listMyCrops(Long farmerUserId);
 
}
 












