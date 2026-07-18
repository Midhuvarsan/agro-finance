package com.agrofinance.dto;
 
import java.math.BigDecimal;
 
public record OfficerDashboardResponse(
        long pendingQueueCount,
        long totalReviewedByMe,
        long approvedByMe,
        long rejectedByMe,
        long disbursedByMe,
        BigDecimal totalAmountApprovedByMe
) {
}
 


































