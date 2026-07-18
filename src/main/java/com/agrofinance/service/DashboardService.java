package com.agrofinance.service;
 
import com.agrofinance.dto.FarmerDashboardResponse;
import com.agrofinance.dto.OfficerDashboardResponse;
 
/**
 * Dedicated to statistics-only reads, separate from FarmerService/
 * BankOfficerController which own CRUD — same separation AdminService's
 * dashboard() already established for admins.
 */
public interface DashboardService {
 
    FarmerDashboardResponse farmerDashboard(Long farmerUserId);
 
    OfficerDashboardResponse officerDashboard(Long officerUserId);
 
}
 


































