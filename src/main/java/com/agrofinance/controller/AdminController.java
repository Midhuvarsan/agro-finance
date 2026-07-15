package com.agrofinance.controller;
 
import com.agrofinance.dto.AdminUserResponse;
import com.agrofinance.dto.DashboardResponse;
import com.agrofinance.dto.LoanResponse;
import com.agrofinance.dto.OfficerAdminResponse;
import com.agrofinance.dto.PageResponse;
import com.agrofinance.dto.UserStatusRequest;
import com.agrofinance.entity.LoanStatus;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.AdminService;
import com.agrofinance.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import java.time.LocalDate;
import java.util.List;
 
/**
 * Class-level ADMIN gate — every endpoint here is admin-only.
 * (Loan scheme management already lives in LoanSchemeController.)
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
 
    private final AdminService adminService;
    private final LoanService loanService;
 
    /**
     * Pageable is resolved by Spring automatically from query params:
     * /api/admin/users?page=0&size=20&sort=createdAt,desc
     * @PageableDefault caps the no-param case at a sane size.
     */
    @GetMapping("/users")
    public PageResponse<AdminUserResponse> listUsers(@PageableDefault(size = 20) Pageable pageable) {
        return adminService.listUsers(pageable);
    }
 
    /**
     * PATCH, not PUT — we're modifying one field of the resource, not
     * replacing the whole representation. Correct verb semantics.
     */
    @PatchMapping("/users/{userId}/status")
    public AdminUserResponse updateUserStatus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusRequest request
    ) {
        return adminService.updateUserStatus(principal.getUser().getId(), userId, request.status());
    }
 
    @GetMapping("/officers")
    public List<OfficerAdminResponse> listOfficers() {
        return adminService.listOfficers();
    }
 
    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return adminService.dashboard();
    }
 
    /** e.g. /api/admin/reports/loans?status=DISBURSED&from=2026-07-01&to=2026-07-31 */
    @GetMapping("/reports/loans")
    public List<LoanResponse> loanReport(
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return loanService.report(status, from, to);
    }
 
}
 


























