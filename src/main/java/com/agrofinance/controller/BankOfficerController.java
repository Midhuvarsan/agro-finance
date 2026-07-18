package com.agrofinance.controller;
 
import com.agrofinance.dto.BankOfficerRequest;
import com.agrofinance.dto.OfficerDashboardResponse;
import com.agrofinance.entity.BankOfficer;
import com.agrofinance.repository.BankOfficerRepository;
import com.agrofinance.repository.UserRepository;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
 
@RestController
@RequestMapping("/api/officers")
@RequiredArgsConstructor
public class BankOfficerController {
 
    private final BankOfficerRepository bankOfficerRepository;
    private final UserRepository userRepository;
    private final DashboardService dashboardService;
 
    @PreAuthorize("hasRole('BANK_OFFICER')")
    @PostMapping("/me")
    @Transactional
    public ResponseEntity<Void> completeMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BankOfficerRequest request
    ) {
        Long userId = principal.getUser().getId();
 
        if (bankOfficerRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists");
        }
 
        BankOfficer officer = new BankOfficer();
        officer.setUser(userRepository.getReferenceById(userId));
        officer.setEmployeeId(request.employeeId());
        officer.setBranchName(request.branchName());
        bankOfficerRepository.save(officer);
 
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
 
    /** NEW (Phase 11): statistics-only dashboard for the logged-in officer. */
    @PreAuthorize("hasRole('BANK_OFFICER')")
    @GetMapping("/me/dashboard")
    public OfficerDashboardResponse getMyDashboard(@AuthenticationPrincipal CustomUserDetails principal) {
        return dashboardService.officerDashboard(principal.getUser().getId());
    }
 
}
 


































