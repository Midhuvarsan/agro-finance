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
 
}
 


























