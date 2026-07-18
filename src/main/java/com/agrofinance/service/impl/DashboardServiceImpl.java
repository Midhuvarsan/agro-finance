package com.agrofinance.service.impl;
 
import com.agrofinance.dto.FarmerDashboardResponse;
import com.agrofinance.dto.OfficerDashboardResponse;
import com.agrofinance.entity.LoanStatus;
import com.agrofinance.repository.CropRepository;
import com.agrofinance.repository.LandRepository;
import com.agrofinance.repository.LoanRepository;
import com.agrofinance.repository.NotificationRepository;
import com.agrofinance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
 
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
 
    private final LoanRepository loanRepository;
    private final LandRepository landRepository;
    private final CropRepository cropRepository;
    private final NotificationRepository notificationRepository;
 
    @Override
    @Transactional(readOnly = true)
    public FarmerDashboardResponse farmerDashboard(Long farmerUserId) {
 
        // Every status present with 0 default — same reasoning as the
        // admin dashboard: a frontend shouldn't have to null-check
        // statuses the farmer simply hasn't hit yet.
        Map<String, Long> loansByStatus = new HashMap<>();
        for (LoanStatus s : LoanStatus.values()) {
            loansByStatus.put(s.name(), 0L);
        }
        for (Object[] row : loanRepository.countGroupedByStatusForFarmer(farmerUserId)) {
            loansByStatus.put(((LoanStatus) row[0]).name(), (Long) row[1]);
        }
 
        return new FarmerDashboardResponse(
                loanRepository.countByFarmerUserId(farmerUserId),
                loansByStatus,
                landRepository.sumAreaByFarmerUserId(farmerUserId),
                cropRepository.countByLandFarmerUserId(farmerUserId),
                notificationRepository.countByUserIdAndReadFalse(farmerUserId)
        );
    }
 
    @Override
    @Transactional(readOnly = true)
    public OfficerDashboardResponse officerDashboard(Long officerUserId) {
 
        long approved = 0, rejected = 0, disbursed = 0;
        for (Object[] row : loanRepository.countGroupedByStatusForOfficer(officerUserId)) {
            LoanStatus status = (LoanStatus) row[0];
            long count = (Long) row[1];
            // BANK_APPROVED and DISBURSED both originate from an "approve"
            // decision by this officer — count both toward approvedByMe,
            // while disbursedByMe tracks the subset that's since been paid out.
            if (status == LoanStatus.BANK_APPROVED || status == LoanStatus.DISBURSED) {
                approved += count;
            }
            if (status == LoanStatus.DISBURSED) {
                disbursed = count;
            }
            if (status == LoanStatus.REJECTED) {
                rejected = count;
            }
        }
 
        BigDecimal totalAmount = loanRepository.sumApprovedAmountByOfficer(officerUserId);
 
        return new OfficerDashboardResponse(
                loanRepository.countByStatus(LoanStatus.PENDING),
                approved + rejected,
                approved,
                rejected,
                disbursed,
                totalAmount
        );
    }
 
}
 


































