package com.agrofinance.dto;
 
import java.math.BigDecimal;
import java.util.Map;
 
public record DashboardResponse(
        long totalUsers,
        long totalFarmers,
        long totalOfficers,
        Map<String, Long> loansByStatus,
        BigDecimal totalDisbursedAmount
) {
}
 


























