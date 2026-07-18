package com.agrofinance.service.impl;
 
import com.agrofinance.dto.LoanApplicationRequest;
import com.agrofinance.dto.LoanDecisionRequest;
import com.agrofinance.dto.LoanResponse;
import com.agrofinance.entity.BankOfficer;
import com.agrofinance.entity.Farmer;
import com.agrofinance.entity.Loan;
import com.agrofinance.entity.LoanScheme;
import com.agrofinance.entity.LoanStatus;
import com.agrofinance.repository.BankOfficerRepository;
import com.agrofinance.repository.FarmerRepository;
import com.agrofinance.repository.LoanRepository;
import com.agrofinance.repository.LoanSchemeRepository;
import com.agrofinance.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
 
import java.util.List;
import java.util.Map;
import java.util.Set;
 
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
 
    /**
     * THE state machine — the transition enforcement promised since
     * Phase 2. Every status change in this service must pass through
     * requireTransition(), which consults this single map. One place to
     * read the whole workflow; one place to change it.
     *
     * NOTE: officers may currently review from PENDING as well as
     * AI_REVIEWED — the AI step (Phase 6, Gemini) will slot in as the
     * PENDING -> AI_REVIEWED transition; until it exists, blocking
     * officers on a state nothing can produce would deadlock the
     * workflow. Deliberate, temporary, and documented.
     */
    private static final Map<LoanStatus, Set<LoanStatus>> ALLOWED_TRANSITIONS = Map.of(
            LoanStatus.PENDING, Set.of(LoanStatus.AI_REVIEWED, LoanStatus.BANK_APPROVED, LoanStatus.REJECTED, LoanStatus.CANCELLED),
            LoanStatus.AI_REVIEWED, Set.of(LoanStatus.BANK_APPROVED, LoanStatus.REJECTED, LoanStatus.CANCELLED),
            LoanStatus.BANK_APPROVED, Set.of(LoanStatus.DISBURSED),
            LoanStatus.REJECTED, Set.of(),
            LoanStatus.CANCELLED, Set.of(),
            LoanStatus.DISBURSED, Set.of()
    );
 
    /** Statuses that count as "an application currently in flight". */
    private static final List<LoanStatus> ACTIVE_STATUSES =
            List.of(LoanStatus.PENDING, LoanStatus.AI_REVIEWED, LoanStatus.BANK_APPROVED);
 
    private final LoanRepository loanRepository;
    private final LoanSchemeRepository loanSchemeRepository;
    private final FarmerRepository farmerRepository;
    private final BankOfficerRepository bankOfficerRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
 
    // ------------------------------------------------------------------
    // Farmer side
    // ------------------------------------------------------------------
 
    @Override
    @Transactional
    public LoanResponse apply(Long farmerUserId, LoanApplicationRequest request) {
 
        Farmer farmer = farmerRepository.findById(farmerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complete your profile first"));
 
        if (loanRepository.existsByFarmerUserIdAndStatusIn(farmerUserId, ACTIVE_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You already have an active loan application");
        }
 
        LoanScheme scheme = loanSchemeRepository.findById(request.loanSchemeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown loan scheme"));
 
        validateAmountAgainstScheme(request.amountRequested(), scheme);
 
        Loan loan = new Loan();
        loan.setFarmer(farmer);
        loan.setLoanScheme(scheme);
        loan.setAmountRequested(request.amountRequested());
        loan.setPurpose(request.purpose());
        loan.setStatus(LoanStatus.PENDING);
 
        return toResponse(loanRepository.save(loan));
    }
 
    @Override
    @Transactional
    public LoanResponse updateApplication(Long farmerUserId, Long loanId, LoanApplicationRequest request) {
 
        Loan loan = ownedLoan(farmerUserId, loanId);
 
        // Editing is only legal BEFORE review begins — once reviewed,
        // cancel-and-reapply is the path, not silently changing what
        // the reviewer already looked at.
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a PENDING application can be edited");
        }
 
        LoanScheme scheme = loanSchemeRepository.findById(request.loanSchemeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown loan scheme"));
        validateAmountAgainstScheme(request.amountRequested(), scheme);
 
        loan.setLoanScheme(scheme);
        loan.setAmountRequested(request.amountRequested());
        loan.setPurpose(request.purpose());
        // dirty checking persists the changes
 
        return toResponse(loan);
    }
 
    @Override
    @Transactional(readOnly = true)
    public LoanResponse getMyLoan(Long farmerUserId, Long loanId) {
        return toResponse(ownedLoan(farmerUserId, loanId));
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> myLoanHistory(Long farmerUserId) {
        return loanRepository.findAllByFarmerUserId(farmerUserId).stream()
                .map(this::toResponse)
                .toList();
    }
 
    @Override
    @Transactional
    public LoanResponse cancel(Long farmerUserId, Long loanId) {
        Loan loan = ownedLoan(farmerUserId, loanId);
 
        // BANK_APPROVED -> CANCELLED is absent from the transition map,
        // so requireTransition blocks post-approval cancellation too —
        // but we check explicitly first for a clearer error message.
        if (loan.getStatus() == LoanStatus.BANK_APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An approved loan cannot be cancelled — contact your branch");
        }
 
        requireTransition(loan, LoanStatus.CANCELLED);
        loan.setStatus(LoanStatus.CANCELLED);
        return toResponse(loan);
    }
 
    // ------------------------------------------------------------------
    // Officer side
    // ------------------------------------------------------------------
 
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> reviewQueue(LoanStatus status) {
        // Null-safe default: no filter supplied = the classic "what
        // needs my attention" PENDING queue.
        LoanStatus effective = (status != null) ? status : LoanStatus.PENDING;
        return loanRepository.findAllByStatus(effective).stream()
                .map(this::toResponse)
                .toList();
    }
 
    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanForReview(Long loanId) {
        return toResponse(loanOrNotFound(loanId));
    }
 
    @Override
    @Transactional
    public LoanResponse approve(Long officerUserId, Long loanId, LoanDecisionRequest decision) {
 
        if (decision.amountApproved() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved amount is required");
        }
 
        Loan loan = loanOrNotFound(loanId);
        requireTransition(loan, LoanStatus.BANK_APPROVED);
 
        if (decision.amountApproved().compareTo(loan.getAmountRequested()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Approved amount cannot exceed the requested amount");
        }
 
        loan.setStatus(LoanStatus.BANK_APPROVED);
        loan.setAmountApproved(decision.amountApproved());
        loan.setOfficerRemarks(decision.remarks());
        loan.setReviewedBy(officerRef(officerUserId));
 
        eventPublisher.publishEvent(new com.agrofinance.event.LoanDecidedEvent(
                loan.getId(), loan.getFarmer().getUserId(), true,
                decision.amountApproved(), decision.remarks(), loan.getLoanScheme().getName()));
 
        return toResponse(loan);
    }
 
    @Override
    @Transactional
    public LoanResponse reject(Long officerUserId, Long loanId, LoanDecisionRequest decision) {
 
        // A rejection with no stated reason is hostile to the farmer and
        // useless for audit — remarks are REQUIRED here, unlike approve.
        if (decision.remarks() == null || decision.remarks().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Remarks are required when rejecting an application");
        }
 
        Loan loan = loanOrNotFound(loanId);
        requireTransition(loan, LoanStatus.REJECTED);
 
        loan.setStatus(LoanStatus.REJECTED);
        loan.setOfficerRemarks(decision.remarks());
        loan.setReviewedBy(officerRef(officerUserId));
 
        eventPublisher.publishEvent(new com.agrofinance.event.LoanDecidedEvent(
                loan.getId(), loan.getFarmer().getUserId(), false,
                null, decision.remarks(), loan.getLoanScheme().getName()));
 
        return toResponse(loan);
    }
 
    @Override
    @Transactional
    public LoanResponse disburse(Long officerUserId, Long loanId) {
        Loan loan = loanOrNotFound(loanId);
        requireTransition(loan, LoanStatus.DISBURSED);
 
        loan.setStatus(LoanStatus.DISBURSED);
        return toResponse(loan);
    }
 
    // ------------------------------------------------------------------
    // Admin reporting
    // ------------------------------------------------------------------
 
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> report(LoanStatus status, java.time.LocalDate from, java.time.LocalDate to) {
        // LocalDate params become full-day timestamp bounds:
        // from = start of that day, to = end of that day (inclusive).
        java.time.LocalDateTime fromTs = (from != null) ? from.atStartOfDay() : null;
        java.time.LocalDateTime toTs = (to != null) ? to.atTime(java.time.LocalTime.MAX) : null;
 
        return loanRepository.findForReport(status, fromTs, toTs).stream()
                .map(this::toResponse)
                .toList();
    }
 
    @Override
    @Transactional
    public void markAiReviewed(Long loanId) {
        Loan loan = loanOrNotFound(loanId);
        requireTransition(loan, LoanStatus.AI_REVIEWED);
        loan.setStatus(LoanStatus.AI_REVIEWED);
        // dirty checking persists
    }
 
    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------
 
    /** The single gate every status change goes through. */
    private void requireTransition(Loan loan, LoanStatus target) {
        Set<LoanStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(loan.getStatus(), Set.of());
        if (!allowed.contains(target)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot move loan from " + loan.getStatus() + " to " + target);
        }
    }
 
    private void validateAmountAgainstScheme(java.math.BigDecimal amount, LoanScheme scheme) {
        if (scheme.getMinAmount() != null && amount.compareTo(scheme.getMinAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount is below this scheme's minimum of " + scheme.getMinAmount());
        }
        if (scheme.getMaxAmount() != null && amount.compareTo(scheme.getMaxAmount()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount exceeds this scheme's maximum of " + scheme.getMaxAmount());
        }
    }
 
    private Loan ownedLoan(Long farmerUserId, Long loanId) {
        return loanRepository.findByIdAndFarmerUserId(loanId, farmerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }
 
    private Loan loanOrNotFound(Long loanId) {
        return loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }
 
    private BankOfficer officerRef(Long officerUserId) {
        return bankOfficerRepository.findById(officerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Complete your bank officer profile first"));
    }
 
    private LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getFarmer().getUserId(),
                loan.getFarmer().getFullName(),
                loan.getLoanScheme().getName(),
                loan.getAmountRequested(),
                loan.getAmountApproved(),
                loan.getStatus(),
                loan.getPurpose(),
                loan.getOfficerRemarks(),
                loan.getReviewedBy() != null ? loan.getReviewedBy().getEmployeeId() : null,
                loan.getCreatedAt(),
                loan.getUpdatedAt()
        );
    }
 
}
 
































