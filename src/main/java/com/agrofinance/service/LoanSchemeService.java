package com.agrofinance.service;
 
import com.agrofinance.dto.LoanSchemeRequest;
import com.agrofinance.dto.LoanSchemeResponse;
 
import java.util.List;
 
public interface LoanSchemeService {
 
    LoanSchemeResponse create(LoanSchemeRequest request);
 
    LoanSchemeResponse update(Long schemeId, LoanSchemeRequest request);
 
    List<LoanSchemeResponse> listAll();
 
}
 