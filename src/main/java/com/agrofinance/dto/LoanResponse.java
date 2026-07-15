package com.agrofinance.dto;
 
import com.agrofinance.entity.LoanStatus;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
public record LoanResponse(
        Long id,
        Long farmerUserId,
        String farmerName,
        String schemeName,
        BigDecimal amountRequested,
        BigDecimal amountApproved,
        LoanStatus status,
        String purpose,
        String officerRemarks,
        String reviewedByOfficer,
        LocalDateTime appliedAt,
        LocalDateTime lastUpdatedAt
) {
}
