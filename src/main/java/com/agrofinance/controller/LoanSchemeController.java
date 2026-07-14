package com.agrofinance.controller;
 
import com.agrofinance.dto.LoanSchemeRequest;
import com.agrofinance.dto.LoanSchemeResponse;
import com.agrofinance.service.LoanSchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/loan-schemes")
@RequiredArgsConstructor
public class LoanSchemeController {
 
    private final LoanSchemeService loanSchemeService;
 
    /** Browsing schemes is for every authenticated user — farmers pick from this list. */
    @GetMapping
    public List<LoanSchemeResponse> listAll() {
        return loanSchemeService.listAll();
    }
 
    /** Creating/editing loan PRODUCTS is admin-only. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LoanSchemeResponse> create(@Valid @RequestBody LoanSchemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanSchemeService.create(request));
    }
 
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{schemeId}")
    public LoanSchemeResponse update(@PathVariable Long schemeId, @Valid @RequestBody LoanSchemeRequest request) {
        return loanSchemeService.update(schemeId, request);
    }
 
}
 




















