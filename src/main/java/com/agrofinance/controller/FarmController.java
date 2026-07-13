package com.agrofinance.controller;
 
import com.agrofinance.dto.CropRequest;
import com.agrofinance.dto.CropResponse;
import com.agrofinance.dto.LandRequest;
import com.agrofinance.dto.LandResponse;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/farm")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FARMER')") // class-level: every endpoint here is farmer-only
public class FarmController {
 
    private final FarmService farmService;
 
    @PostMapping("/lands")
    public ResponseEntity<LandResponse> addLand(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody LandRequest request
    ) {
        LandResponse response = farmService.addLand(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @GetMapping("/lands")
    public List<LandResponse> listMyLands(@AuthenticationPrincipal CustomUserDetails principal) {
        return farmService.listMyLands(principal.getUser().getId());
    }
 
    @PostMapping("/crops")
    public ResponseEntity<CropResponse> addCrop(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CropRequest request
    ) {
        CropResponse response = farmService.addCrop(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @GetMapping("/crops")
    public List<CropResponse> listMyCrops(@AuthenticationPrincipal CustomUserDetails principal) {
        return farmService.listMyCrops(principal.getUser().getId());
    }
 
}
 