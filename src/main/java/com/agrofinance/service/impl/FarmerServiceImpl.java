package com.agrofinance.service.impl;
 
import com.agrofinance.dto.FarmerRequest;
import com.agrofinance.dto.FarmerResponse;
import com.agrofinance.dto.FarmerReviewResponse;
import com.agrofinance.entity.Farmer;
import com.agrofinance.entity.User;
import com.agrofinance.entity.UserStatus;
import com.agrofinance.repository.FarmerRepository;
import com.agrofinance.repository.UserRepository;
import com.agrofinance.service.DocumentService;
import com.agrofinance.service.FarmService;
import com.agrofinance.service.FarmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
 
import java.time.LocalDateTime;
import java.util.Objects;
 
@Service
@RequiredArgsConstructor
public class FarmerServiceImpl implements FarmerService {
 
    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;
    private final FarmService farmService;
    private final DocumentService documentService;
 
    @Override
    @Transactional
    public FarmerResponse createProfile(Long userId, FarmerRequest request) {
 
        if (farmerRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists — use update instead");
        }
        if (farmerRepository.existsByAadhaarNumber(request.aadhaarNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This Aadhaar number is already registered");
        }
 
        // Lazy proxy, no SELECT — we already know this user exists
        // (they're authenticated) and only need their identity for the
        // @MapsId link, not any of their actual field values.
        User userRef = userRepository.getReferenceById(userId);
 
        Farmer farmer = new Farmer();
        farmer.setUser(userRef);
        farmer.setFullName(request.fullName());
        farmer.setAadhaarNumber(request.aadhaarNumber());
        farmer.setDateOfBirth(request.dateOfBirth());
        farmer.setAddress(request.address());
 
        Farmer saved = farmerRepository.save(farmer);
        return toResponse(saved);
    }
 
    @Override
    @Transactional
    public FarmerResponse updateProfile(Long userId, FarmerRequest request) {
 
        Farmer farmer = farmerRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complete your profile first"));
 
        // Only re-check uniqueness if the Aadhaar is actually changing —
        // otherwise this would always "conflict" with the farmer's own
        // existing (unchanged) value.
        if (!Objects.equals(farmer.getAadhaarNumber(), request.aadhaarNumber())
                && farmerRepository.existsByAadhaarNumber(request.aadhaarNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This Aadhaar number is already registered");
        }
 
        farmer.setFullName(request.fullName());
        farmer.setAadhaarNumber(request.aadhaarNumber());
        farmer.setDateOfBirth(request.dateOfBirth());
        farmer.setAddress(request.address());
 
        // No explicit save() — `farmer` is a MANAGED entity inside this
        // @Transactional method. Hibernate's dirty checking detects the
        // setter calls above and flushes them automatically at commit.
        return toResponse(farmer);
    }
 
    @Override
    @Transactional(readOnly = true)
    public FarmerResponse getProfile(Long userId) {
        Farmer farmer = farmerRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer profile not found"));
        return toResponse(farmer);
    }
 
    @Override
    @Transactional
    public void deactivateProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 
        // Soft delete, same pattern established for User back in Phase 2
        // — never a real DELETE, which would also be blocked by the
        // NOT NULL foreign key on any existing Loan.farmer_id anyway.
        user.setStatus(UserStatus.INACTIVE);
        user.setDeletedAt(LocalDateTime.now());
        // Again, no explicit save() — dirty checking handles it.
    }
 
    @Override
    @Transactional(readOnly = true)
    public FarmerReviewResponse getReviewDetails(Long userId) {
        // Composes existing service methods instead of duplicating their
        // repository queries and DTO mapping — one source of truth per
        // domain. The "listMy*" names read oddly here (the caller is an
        // officer, not the farmer) but the semantics are identical:
        // everything belonging to this farmer's userId.
        FarmerResponse profile = getProfile(userId);
        return new FarmerReviewResponse(
                profile,
                farmService.listMyLands(userId),
                farmService.listMyCrops(userId),
                documentService.listMyDocuments(userId)
        );
    }
 
    private FarmerResponse toResponse(Farmer farmer) {
        return new FarmerResponse(
                farmer.getUserId(),
                farmer.getUser().getEmail(),
                farmer.getFullName(),
                farmer.getAadhaarNumber(),
                farmer.getDateOfBirth(),
                farmer.getAddress()
        );
    }
 
}
 


























