package com.agrofinance.dto;
 
import java.math.BigDecimal;
import java.util.Map;
 
public record FarmerDashboardResponse(
        long totalLoans,
        Map<String, Long> loansByStatus,
        BigDecimal totalLandAcres,
        long totalCrops,
        long unreadNotifications
) {
}
