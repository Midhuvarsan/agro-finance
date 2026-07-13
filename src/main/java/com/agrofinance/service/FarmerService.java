package com.agrofinance.service;
 
import com.agrofinance.dto.FarmerRequest;
import com.agrofinance.dto.FarmerResponse;
 
public interface FarmerService {
 
    FarmerResponse createProfile(Long userId, FarmerRequest request);
 
    FarmerResponse updateProfile(Long userId, FarmerRequest request);
 
    FarmerResponse getProfile(Long userId);
 
    void deactivateProfile(Long userId);
 
}