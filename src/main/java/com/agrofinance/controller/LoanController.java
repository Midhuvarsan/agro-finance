package com.agrofinance.controller;
 
import com.agrofinance.dto.LoanApplicationRequest;
import com.agrofinance.dto.LoanDecisionRequest;
import com.agrofinance.dto.LoanResponse;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.LoanService;
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
 
import java.util.List;
 
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
 
    private final LoanService loanService;
 
    // ---------------- Farmer endpoints ----------------
 
    @PreAuthorize("hasRole('FARMER')")
    @PostMapping
    public ResponseEntity<LoanResponse> apply(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody LoanApplicationRequest request
    ) {
        LoanResponse response = loanService.apply(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/my")
    public List<LoanResponse> myHistory(@AuthenticationPrincipal CustomUserDetails principal) {
        return loanService.myLoanHistory(principal.getUser().getId());
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/my/{loanId}")
    public LoanResponse myLoan(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long loanId
    ) {
        return loanService.getMyLoan(principal.getUser().getId(), loanId);
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @PutMapping("/my/{loanId}")
    public LoanResponse updateMyLoan(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long loanId,
            @Valid @RequestBody LoanApplicationRequest request
    ) {
        return loanService.updateApplication(principal.getUser().getId(), loanId, request);
    }
 
    /**
     * DELETE verb, but semantically a cancellation (status change) —
     * the loan record remains, per the "cancel is a status, not a
     * delete" decision in Step 1.
     */
    @PreAuthorize("hasRole('FARMER')")
    @DeleteMapping("/my/{loanId}")
    public LoanResponse cancelMyLoan(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long loanId
    ) {
        return loanService.cancel(principal.getUser().getId(), loanId);
    }
 
    // ---------------- Officer endpoints ----------------
 
    @PreAuthorize("hasRole('BANK_OFFICER')")
    @GetMapping("/review-queue")
    public List<LoanResponse> reviewQueue() {
        return loanService.reviewQueue();
    }
 
    @PreAuthorize("hasAnyRole('BANK_OFFICER', 'ADMIN')")
    @GetMapping("/{loanId}")
    public LoanResponse getLoan(@PathVariable Long loanId) {
        return loanService.getLoanForReview(loanId);
    }
 
    @PreAuthorize("hasRole('BANK_OFFICER')")
    @PostMapping("/{loanId}/approve")
    public LoanResponse approve(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long loanId,
            @Valid @RequestBody LoanDecisionRequest decision
    ) {
        return loanService.approve(principal.getUser().getId(), loanId, decision);
    }
 
    @PreAuthorize("hasRole('BANK_OFFICER')")
    @PostMapping("/{loanId}/reject")
    public LoanResponse reject(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long loanId,
            @Valid @RequestBody LoanDecisionRequest decision
    ) {
        return loanService.reject(principal.getUser().getId(), loanId, decision);
    }
 
    @PreAuthorize("hasRole('BANK_OFFICER')")
    @PostMapping("/{loanId}/disburse")
    public LoanResponse disburse(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long loanId
    ) {
        return loanService.disburse(principal.getUser().getId(), loanId);
    }
 
}
 




















