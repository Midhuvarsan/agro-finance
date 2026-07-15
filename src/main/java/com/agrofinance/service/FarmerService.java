package com.agrofinance.service;
 
import com.agrofinance.dto.FarmerRequest;
import com.agrofinance.dto.FarmerResponse;
import com.agrofinance.dto.FarmerReviewResponse;
 
public interface FarmerService {
 
    FarmerResponse createProfile(Long userId, FarmerRequest request);
 
    FarmerResponse updateProfile(Long userId, FarmerRequest request);
 
    FarmerResponse getProfile(Long userId);
 
    void deactivateProfile(Long userId);
 
    /** Composite view for loan reviewers: profile + lands + crops + documents. */
    FarmerReviewResponse getReviewDetails(Long userId);
 
}
 


























