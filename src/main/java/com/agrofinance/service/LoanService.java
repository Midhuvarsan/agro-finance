package com.agrofinance.service;
 
import com.agrofinance.dto.LoanApplicationRequest;
import com.agrofinance.dto.LoanDecisionRequest;
import com.agrofinance.dto.LoanResponse;
 
import java.util.List;
 
public interface LoanService {
 
    // ---- Farmer side ----
    LoanResponse apply(Long farmerUserId, LoanApplicationRequest request);
 
    LoanResponse updateApplication(Long farmerUserId, Long loanId, LoanApplicationRequest request);
 
    LoanResponse getMyLoan(Long farmerUserId, Long loanId);
 
    List<LoanResponse> myLoanHistory(Long farmerUserId);
 
    LoanResponse cancel(Long farmerUserId, Long loanId);
 
    // ---- Officer side ----
    List<LoanResponse> reviewQueue(com.agrofinance.entity.LoanStatus status);
 
    LoanResponse getLoanForReview(Long loanId);
 
    LoanResponse approve(Long officerUserId, Long loanId, LoanDecisionRequest decision);
 
    LoanResponse reject(Long officerUserId, Long loanId, LoanDecisionRequest decision);
 
    LoanResponse disburse(Long officerUserId, Long loanId);
 
    // ---- Admin reporting ----
    List<LoanResponse> report(com.agrofinance.entity.LoanStatus status,
                              java.time.LocalDate from,
                              java.time.LocalDate to);
 
    // ---- AI integration hook ----
    /**
     * Marks a loan AI-reviewed, enforcing the PENDING -> AI_REVIEWED
     * transition. Exists so GeminiAIService can move the workflow
     * WITHOUT owning any state-machine logic — transitions live in
     * exactly one class (LoanServiceImpl), always.
     */
    void markAiReviewed(Long loanId);
 
}
 






























