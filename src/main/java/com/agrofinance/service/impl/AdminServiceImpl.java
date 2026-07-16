package com.agrofinance.service.impl;
 
import com.agrofinance.dto.AdminUserResponse;
import com.agrofinance.dto.DashboardResponse;
import com.agrofinance.dto.OfficerAdminResponse;
import com.agrofinance.dto.PageResponse;
import com.agrofinance.entity.LoanStatus;
import com.agrofinance.entity.Role;
import com.agrofinance.entity.User;
import com.agrofinance.entity.UserStatus;
import com.agrofinance.repository.BankOfficerRepository;
import com.agrofinance.repository.FarmerRepository;
import com.agrofinance.repository.LoanRepository;
import com.agrofinance.repository.UserRepository;
import com.agrofinance.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
 
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final BankOfficerRepository bankOfficerRepository;
    private final LoanRepository loanRepository;
 
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> listUsers(Pageable pageable) {
        return PageResponse.from(
                userRepository.findAll(pageable).map(this::toUserResponse)
        );
    }
 
    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(Long adminUserId, Long targetUserId, UserStatus status) {
 
        // Guard: an admin locking their own account out (or the last
        // admin suspending themselves) is an operational foot-gun.
        if (adminUserId.equals(targetUserId) && status != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You cannot suspend or deactivate your own account");
        }
 
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 
        user.setStatus(status);
        // Reactivation clears a prior soft-delete; suspension does NOT
        // set deletedAt (suspended != deleted — different semantics).
        if (status == UserStatus.ACTIVE) {
            user.setDeletedAt(null);
        } else if (status == UserStatus.INACTIVE) {
            user.setDeletedAt(LocalDateTime.now());
        }
        // dirty checking persists
 
        return toUserResponse(user);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<OfficerAdminResponse> listOfficers() {
        return bankOfficerRepository.findAllWithUser().stream()
                .map(o -> new OfficerAdminResponse(
                        o.getUserId(),
                        o.getUser().getEmail(),
                        o.getEmployeeId(),
                        o.getBranchName()))
                .toList();
    }
 
    /**
     * TTL-only caching (60s, set in CacheConfig) — no @CacheEvict
     * anywhere for this cache, deliberately: every loan application,
     * approval, and registration changes these numbers, so event-based
     * eviction would fire constantly and the cache would never help.
     * A minute of staleness on an admin dashboard is the right trade.
     */
    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(cacheNames = com.agrofinance.constants.CacheNames.DASHBOARD)
    public DashboardResponse dashboard() {
 
        Map<String, Long> loansByStatus = new HashMap<>();
        // Every status present with 0 default — a dashboard with missing
        // keys forces every frontend to null-check per status.
        for (LoanStatus s : LoanStatus.values()) {
            loansByStatus.put(s.name(), 0L);
        }
        for (Object[] row : loanRepository.countGroupedByStatus()) {
            loansByStatus.put(((LoanStatus) row[0]).name(), (Long) row[1]);
        }
 
        BigDecimal totalDisbursed = loanRepository.sumApprovedAmountByStatus(LoanStatus.DISBURSED);
 
        return new DashboardResponse(
                userRepository.count(),
                farmerRepository.count(),
                bankOfficerRepository.count(),
                loansByStatus,
                totalDisbursed
        );
    }
 
    private AdminUserResponse toUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
 
}
 
































