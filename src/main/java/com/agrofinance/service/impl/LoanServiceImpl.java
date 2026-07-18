package com.agrofinance.service.impl;
 
import com.agrofinance.dto.LoanApplicationRequest;
import com.agrofinance.dto.LoanDecisionRequest;
import com.agrofinance.dto.LoanResponse;
import com.agrofinance.entity.BankOfficer;
import com.agrofinance.entity.Farmer;
import com.agrofinance.entity.Loan;
import com.agrofinance.entity.LoanScheme;
import com.agrofinance.entity.LoanStatus;
import com.agrofinance.event.LoanDecidedEvent;
import com.agrofinance.exception.BadRequestException;
import com.agrofinance.exception.InvalidOperationException;
import com.agrofinance.exception.ResourceNotFoundException;
import com.agrofinance.repository.BankOfficerRepository;
import com.agrofinance.repository.FarmerRepository;
import com.agrofinance.repository.LoanRepository;
import com.agrofinance.repository.LoanSchemeRepository;
import com.agrofinance.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
 
    /** THE state machine — every status change funnels through requireTransition(). */
    private static final Map<LoanStatus, Set<LoanStatus>> ALLOWED_TRANSITIONS = Map.of(
            LoanStatus.PENDING, Set.of(LoanStatus.AI_REVIEWED, LoanStatus.BANK_APPROVED, LoanStatus.REJECTED, LoanStatus.CANCELLED),
            LoanStatus.AI_REVIEWED, Set.of(LoanStatus.BANK_APPROVED, LoanStatus.REJECTED, LoanStatus.CANCELLED),
            LoanStatus.BANK_APPROVED, Set.of(LoanStatus.DISBURSED),
            LoanStatus.REJECTED, Set.of(),
            LoanStatus.CANCELLED, Set.of(),
            LoanStatus.DISBURSED, Set.of()
    );
 
    private static final List<LoanStatus> ACTIVE_STATUSES =
            List.of(LoanStatus.PENDING, LoanStatus.AI_REVIEWED, LoanStatus.BANK_APPROVED);
 
    private final LoanRepository loanRepository;
    private final LoanSchemeRepository loanSchemeRepository;
    private final FarmerRepository farmerRepository;
    private final BankOfficerRepository bankOfficerRepository;
    private final ApplicationEventPublisher eventPublisher;
 
    // ---- Farmer side ----
 
    @Override
    @Transactional
    public LoanResponse apply(Long farmerUserId, LoanApplicationRequest request) {
 
        Farmer farmer = farmerRepository.findById(farmerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Complete your profile first"));
 
        if (loanRepository.existsByFarmerUserIdAndStatusIn(farmerUserId, ACTIVE_STATUSES)) {
            throw new InvalidOperationException("You already have an active loan application");
        }
 
        LoanScheme scheme = loanSchemeRepository.findById(request.loanSchemeId())
                .orElseThrow(() -> new BadRequestException("Unknown loan scheme"));
 
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
 
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new InvalidOperationException("Only a PENDING application can be edited");
        }
 
        LoanScheme scheme = loanSchemeRepository.findById(request.loanSchemeId())
                .orElseThrow(() -> new BadRequestException("Unknown loan scheme"));
        validateAmountAgainstScheme(request.amountRequested(), scheme);
 
        loan.setLoanScheme(scheme);
        loan.setAmountRequested(request.amountRequested());
        loan.setPurpose(request.purpose());
 
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
 
        if (loan.getStatus() == LoanStatus.BANK_APPROVED) {
            throw new InvalidOperationException("An approved loan cannot be cancelled — contact your branch");
        }
 
        requireTransition(loan, LoanStatus.CANCELLED);
        loan.setStatus(LoanStatus.CANCELLED);
        return toResponse(loan);
    }
 
    // ---- Officer side ----
 
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> reviewQueue(LoanStatus status) {
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
            throw new BadRequestException("Approved amount is required");
        }
 
        Loan loan = loanOrNotFound(loanId);
        requireTransition(loan, LoanStatus.BANK_APPROVED);
 
        if (decision.amountApproved().compareTo(loan.getAmountRequested()) > 0) {
            throw new BadRequestException("Approved amount cannot exceed the requested amount");
        }
 
        loan.setStatus(LoanStatus.BANK_APPROVED);
        loan.setAmountApproved(decision.amountApproved());
        loan.setOfficerRemarks(decision.remarks());
        loan.setReviewedBy(officerRef(officerUserId));
 
        eventPublisher.publishEvent(new LoanDecidedEvent(
                loan.getId(), loan.getFarmer().getUserId(), true,
                decision.amountApproved(), decision.remarks(), loan.getLoanScheme().getName()));
 
        return toResponse(loan);
    }
 
    @Override
    @Transactional
    public LoanResponse reject(Long officerUserId, Long loanId, LoanDecisionRequest decision) {
 
        if (decision.remarks() == null || decision.remarks().isBlank()) {
            throw new BadRequestException("Remarks are required when rejecting an application");
        }
 
        Loan loan = loanOrNotFound(loanId);
        requireTransition(loan, LoanStatus.REJECTED);
 
        loan.setStatus(LoanStatus.REJECTED);
        loan.setOfficerRemarks(decision.remarks());
        loan.setReviewedBy(officerRef(officerUserId));
 
        eventPublisher.publishEvent(new LoanDecidedEvent(
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
 
    // ---- Admin reporting ----
 
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> report(LoanStatus status, LocalDate from, LocalDate to) {
        LocalDateTime fromTs = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toTs = (to != null) ? to.atTime(LocalTime.MAX) : null;
        return loanRepository.findForReport(status, fromTs, toTs).stream()
                .map(this::toResponse)
                .toList();
    }
 
    // ---- AI integration hook ----
 
    @Override
    @Transactional
    public void markAiReviewed(Long loanId) {
        Loan loan = loanOrNotFound(loanId);
        requireTransition(loan, LoanStatus.AI_REVIEWED);
        loan.setStatus(LoanStatus.AI_REVIEWED);
    }
 
    // ---- Internals ----
 
    private void requireTransition(Loan loan, LoanStatus target) {
        Set<LoanStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(loan.getStatus(), Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidOperationException(
                    "Cannot move loan from " + loan.getStatus() + " to " + target);
        }
    }
 
    private void validateAmountAgainstScheme(BigDecimal amount, LoanScheme scheme) {
        if (scheme.getMinAmount() != null && amount.compareTo(scheme.getMinAmount()) < 0) {
            throw new BadRequestException("Amount is below this scheme's minimum of " + scheme.getMinAmount());
        }
        if (scheme.getMaxAmount() != null && amount.compareTo(scheme.getMaxAmount()) > 0) {
            throw new BadRequestException("Amount exceeds this scheme's maximum of " + scheme.getMaxAmount());
        }
    }
 
    private Loan ownedLoan(Long farmerUserId, Long loanId) {
        return loanRepository.findByIdAndFarmerUserId(loanId, farmerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }
 
    private Loan loanOrNotFound(Long loanId) {
        return loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }
 
    private BankOfficer officerRef(Long officerUserId) {
        return bankOfficerRepository.findById(officerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Complete your bank officer profile first"));
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
