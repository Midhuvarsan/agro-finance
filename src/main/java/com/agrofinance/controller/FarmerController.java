package com.agrofinance.controller;
 
import com.agrofinance.dto.FarmerDashboardResponse;
import com.agrofinance.dto.FarmerRequest;
import com.agrofinance.dto.FarmerResponse;
import com.agrofinance.dto.FarmerReviewResponse;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.DashboardService;
import com.agrofinance.service.FarmerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api/farmers")
@RequiredArgsConstructor
public class FarmerController {
 
    private final FarmerService farmerService;
    private final DashboardService dashboardService;
 
    @PreAuthorize("hasRole('FARMER')")
    @PostMapping("/me")
    public ResponseEntity<FarmerResponse> createMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody FarmerRequest request
    ) {
        FarmerResponse response = farmerService.createProfile(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @PutMapping("/me")
    public FarmerResponse updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody FarmerRequest request
    ) {
        return farmerService.updateProfile(principal.getUser().getId(), request);
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/me")
    public FarmerResponse getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return farmerService.getProfile(principal.getUser().getId());
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        farmerService.deactivateProfile(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
 
    /** NEW (Phase 11): statistics-only dashboard for the logged-in farmer. */
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/me/dashboard")
    public FarmerDashboardResponse getMyDashboard(@AuthenticationPrincipal CustomUserDetails principal) {
        return dashboardService.farmerDashboard(principal.getUser().getId());
    }
 
    @PreAuthorize("hasAnyRole('BANK_OFFICER', 'ADMIN')")
    @GetMapping("/{userId}")
    public FarmerResponse getFarmerById(@PathVariable Long userId) {
        return farmerService.getProfile(userId);
    }
 
    @PreAuthorize("hasAnyRole('BANK_OFFICER', 'ADMIN')")
    @GetMapping("/{userId}/review")
    public FarmerReviewResponse getFarmerReviewDetails(@PathVariable Long userId) {
        return farmerService.getReviewDetails(userId);
    }
 
}
 


































