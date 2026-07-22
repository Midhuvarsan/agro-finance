package com.agrofinance.service.impl;
 
import com.agrofinance.dto.LoanApplicationRequest;
import com.agrofinance.dto.LoanDecisionRequest;
import com.agrofinance.dto.LoanResponse;
import com.agrofinance.entity.*;
import com.agrofinance.exception.BadRequestException;
import com.agrofinance.exception.InvalidOperationException;
import com.agrofinance.exception.ResourceNotFoundException;
import com.agrofinance.repository.BankOfficerRepository;
import com.agrofinance.repository.FarmerRepository;
import com.agrofinance.repository.LoanRepository;
import com.agrofinance.repository.LoanSchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.ArgumentCaptor;
import com.agrofinance.event.LoanDecidedEvent;
 
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
 
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
 
/**
 * Unit tests for the loan state machine — the single highest-value
 * target for testing in this codebase. Pure Mockito: no Spring context,
 * no database, milliseconds per test. Every repository is mocked so we
 * test ONLY LoanServiceImpl's own decision logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanServiceImpl — state machine and business rules")
class LoanServiceImplTest {
 
    @Mock private LoanRepository loanRepository;
    @Mock private LoanSchemeRepository loanSchemeRepository;
    @Mock private FarmerRepository farmerRepository;
    @Mock private BankOfficerRepository bankOfficerRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
 
    @InjectMocks
    private LoanServiceImpl loanService;
 
    private Farmer farmer;
    private LoanScheme scheme;
    private BankOfficer officer;
 
    @BeforeEach
    void setUp() {
        User farmerUser = new User();
        farmerUser.setId(9L);
        farmerUser.setEmail("farmer7@test.com");
 
        farmer = new Farmer();
        farmer.setUser(farmerUser);
        farmer.setFullName("Ravi Kumar");
 
        scheme = new LoanScheme();
        scheme.setId(1L);
        scheme.setName("Kisan Credit");
        scheme.setMinAmount(new BigDecimal("10000"));
        scheme.setMaxAmount(new BigDecimal("300000"));
 
        User officerUser = new User();
        officerUser.setId(15L);
        officer = new BankOfficer();
        officer.setUser(officerUser);
        officer.setEmployeeId("EMP-001");
    }
 
    private Loan loanWithStatus(LoanStatus status) {
        Loan loan = new Loan();
        loan.setId(100L);
        loan.setFarmer(farmer);
        loan.setLoanScheme(scheme);
        loan.setAmountRequested(new BigDecimal("50000"));
        loan.setStatus(status);
        return loan;
    }
 
    @Nested
    @DisplayName("apply()")
    class Apply {
 
        @Test
        @DisplayName("succeeds and defaults to PENDING when no active loan exists")
        void apply_success() {
            when(farmerRepository.findById(9L)).thenReturn(Optional.of(farmer));
            when(loanRepository.existsByFarmerUserIdAndStatusIn(eq(9L), anyList())).thenReturn(false);
            when(loanSchemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
            when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
                Loan l = inv.getArgument(0);
                l.setId(200L);
                return l;
            });
 
            LoanApplicationRequest request = new LoanApplicationRequest(1L, new BigDecimal("50000"), "Seed purchase");
            LoanResponse response = loanService.apply(9L, request);
 
            assertThat(response.status()).isEqualTo(LoanStatus.PENDING);
            verify(loanRepository).save(any(Loan.class));
        }
 
        @Test
        @DisplayName("rejects a second application while one is already active")
        void apply_activeLoanExists_throws() {
            when(farmerRepository.findById(9L)).thenReturn(Optional.of(farmer));
            when(loanRepository.existsByFarmerUserIdAndStatusIn(eq(9L), anyList())).thenReturn(true);
 
            LoanApplicationRequest request = new LoanApplicationRequest(1L, new BigDecimal("50000"), "test");
 
            assertThatThrownBy(() -> loanService.apply(9L, request))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("already have an active loan");
 
            // Critical: no save should ever happen once the guard fires.
            verify(loanRepository, never()).save(any());
        }
 
        @Test
        @DisplayName("rejects an amount below the scheme's minimum")
        void apply_amountBelowMinimum_throws() {
            when(farmerRepository.findById(9L)).thenReturn(Optional.of(farmer));
            when(loanRepository.existsByFarmerUserIdAndStatusIn(eq(9L), anyList())).thenReturn(false);
            when(loanSchemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
 
            LoanApplicationRequest request = new LoanApplicationRequest(1L, new BigDecimal("5000"), "test"); // below 10000 min
 
            assertThatThrownBy(() -> loanService.apply(9L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("minimum");
        }
 
        @Test
        @DisplayName("rejects an unknown scheme id")
        void apply_unknownScheme_throws() {
            when(farmerRepository.findById(9L)).thenReturn(Optional.of(farmer));
            when(loanRepository.existsByFarmerUserIdAndStatusIn(eq(9L), anyList())).thenReturn(false);
            when(loanSchemeRepository.findById(999L)).thenReturn(Optional.empty());
 
            LoanApplicationRequest request = new LoanApplicationRequest(999L, new BigDecimal("50000"), "test");
 
            assertThatThrownBy(() -> loanService.apply(9L, request))
                    .isInstanceOf(BadRequestException.class);
        }
    }
 
    @Nested
    @DisplayName("approve() — the state machine's transition guard")
    class Approve {
 
        @Test
        @DisplayName("PENDING -> BANK_APPROVED is a legal transition")
        void approve_fromPending_succeeds() {
            Loan loan = loanWithStatus(LoanStatus.PENDING);
            when(loanRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(loan));
            when(bankOfficerRepository.findById(15L)).thenReturn(Optional.of(officer));
 
            LoanDecisionRequest decision = new LoanDecisionRequest(new BigDecimal("45000"), "Looks good");
            LoanResponse response = loanService.approve(15L, 100L, decision);
 
            assertThat(response.status()).isEqualTo(LoanStatus.BANK_APPROVED);
            assertThat(loan.getAmountApproved()).isEqualByComparingTo("45000");
            ArgumentCaptor<LoanDecidedEvent> captor = ArgumentCaptor.forClass(LoanDecidedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().approved()).isTrue();
            assertThat(captor.getValue().amountApproved()).isEqualByComparingTo("45000");
        }
 
        @Test
        @DisplayName("REJECTED -> BANK_APPROVED is illegal (terminal state)")
        void approve_fromTerminalState_throws() {
            Loan loan = loanWithStatus(LoanStatus.REJECTED);
            when(loanRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(loan));
 
            LoanDecisionRequest decision = new LoanDecisionRequest(new BigDecimal("45000"), null);
 
            assertThatThrownBy(() -> loanService.approve(15L, 100L, decision))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("Cannot move loan from REJECTED to BANK_APPROVED");
        }
 
        @Test
        @DisplayName("approved amount cannot exceed the requested amount")
        void approve_amountExceedsRequested_throws() {
            Loan loan = loanWithStatus(LoanStatus.PENDING); // requested = 50000
            when(loanRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(loan));
 
            LoanDecisionRequest decision = new LoanDecisionRequest(new BigDecimal("60000"), null);
 
            assertThatThrownBy(() -> loanService.approve(15L, 100L, decision))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cannot exceed");
        }
 
        @Test
        @DisplayName("missing approved amount is rejected before touching the loan")
        void approve_missingAmount_throws() {
            LoanDecisionRequest decision = new LoanDecisionRequest(null, "remarks only");
 
            assertThatThrownBy(() -> loanService.approve(15L, 100L, decision))
                    .isInstanceOf(BadRequestException.class);
 
            // Fails fast — never even looks the loan up.
            verifyNoInteractions(loanRepository);
        }
    }
 
    @Nested
    @DisplayName("reject()")
    class Reject {
 
        @Test
        @DisplayName("requires remarks — a rejection with no reason is rejected itself")
        void reject_withoutRemarks_throws() {
            LoanDecisionRequest decision = new LoanDecisionRequest(null, "   "); // blank
 
            assertThatThrownBy(() -> loanService.reject(15L, 100L, decision))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Remarks are required");
        }
 
        @Test
        @DisplayName("succeeds with remarks and publishes the decided event")
        void reject_withRemarks_succeeds() {
            Loan loan = loanWithStatus(LoanStatus.PENDING);
            when(loanRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(loan));
            when(bankOfficerRepository.findById(15L)).thenReturn(Optional.of(officer));
 
            LoanResponse response = loanService.reject(15L, 100L, new LoanDecisionRequest(null, "Insufficient collateral"));
 
            assertThat(response.status()).isEqualTo(LoanStatus.REJECTED);
            ArgumentCaptor<LoanDecidedEvent> captor = ArgumentCaptor.forClass(LoanDecidedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().approved()).isFalse();
            assertThat(captor.getValue().remarks()).isEqualTo("Insufficient collateral");
        }
    }
 
    @Nested
    @DisplayName("cancel()")
    class Cancel {
 
        @Test
        @DisplayName("a PENDING loan can be cancelled by the farmer")
        void cancel_fromPending_succeeds() {
            Loan loan = loanWithStatus(LoanStatus.PENDING);
            when(loanRepository.findByIdAndFarmerUserId(100L, 9L)).thenReturn(Optional.of(loan));
 
            LoanResponse response = loanService.cancel(9L, 100L);
 
            assertThat(response.status()).isEqualTo(LoanStatus.CANCELLED);
        }
 
        @Test
        @DisplayName("a BANK_APPROVED loan CANNOT be cancelled — funds are already committed")
        void cancel_afterApproval_throws() {
            Loan loan = loanWithStatus(LoanStatus.BANK_APPROVED);
            when(loanRepository.findByIdAndFarmerUserId(100L, 9L)).thenReturn(Optional.of(loan));
 
            assertThatThrownBy(() -> loanService.cancel(9L, 100L))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("cannot be cancelled");
        }
    }
 
    @Test
    @DisplayName("getMyLoan() throws ResourceNotFoundException, not a generic exception, when the loan doesn't belong to this farmer")
    void getMyLoan_notOwned_throwsResourceNotFound() {
        when(loanRepository.findByIdAndFarmerUserId(999L, 9L)).thenReturn(Optional.empty());
 
        assertThatThrownBy(() -> loanService.getMyLoan(9L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
 
}
 


































