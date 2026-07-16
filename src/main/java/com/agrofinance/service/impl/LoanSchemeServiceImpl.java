package com.agrofinance.service.impl;
 
import com.agrofinance.constants.CacheNames;
import com.agrofinance.dto.LoanSchemeRequest;
import com.agrofinance.dto.LoanSchemeResponse;
import com.agrofinance.entity.LoanScheme;
import com.agrofinance.repository.LoanSchemeRepository;
import com.agrofinance.service.LoanSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class LoanSchemeServiceImpl implements LoanSchemeService {
 
    private final LoanSchemeRepository loanSchemeRepository;
 
    /**
     * Event-based eviction: schemes change rarely and only through
     * these two methods, so the cache is never stale. allEntries=true
     * because listAll's single entry is the whole cache.
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.LOAN_SCHEMES, allEntries = true)
    public LoanSchemeResponse create(LoanSchemeRequest request) {
        validateAmounts(request);
        if (loanSchemeRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A scheme with this name already exists");
        }
 
        LoanScheme scheme = new LoanScheme();
        applyRequest(scheme, request);
        return toResponse(loanSchemeRepository.save(scheme));
    }
 
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.LOAN_SCHEMES, allEntries = true)
    public LoanSchemeResponse update(Long schemeId, LoanSchemeRequest request) {
        validateAmounts(request);
        LoanScheme scheme = loanSchemeRepository.findById(schemeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scheme not found"));
 
        applyRequest(scheme, request); // dirty checking persists this — no save() needed
        return toResponse(scheme);
    }
 
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.LOAN_SCHEMES)
    public List<LoanSchemeResponse> listAll() {
        return loanSchemeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }
 
    /**
     * Cross-field validation (min vs max) can't be expressed by
     * single-field annotations — this is the service-layer's job.
     */
    private void validateAmounts(LoanSchemeRequest request) {
        if (request.maxAmount().compareTo(request.minAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum amount cannot be less than minimum amount");
        }
    }
 
    private void applyRequest(LoanScheme scheme, LoanSchemeRequest request) {
        scheme.setName(request.name());
        scheme.setDescription(request.description());
        scheme.setInterestRate(request.interestRate());
        scheme.setMinAmount(request.minAmount());
        scheme.setMaxAmount(request.maxAmount());
        scheme.setTenureMonths(request.tenureMonths());
    }
 
    private LoanSchemeResponse toResponse(LoanScheme s) {
        return new LoanSchemeResponse(
                s.getId(), s.getName(), s.getDescription(),
                s.getInterestRate(), s.getMinAmount(), s.getMaxAmount(), s.getTenureMonths()
        );
    }
 
}
 
































