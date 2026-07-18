package com.agrofinance.event;
 
import java.math.BigDecimal;
 
/** Published by LoanService when an officer approves or rejects. */
public record LoanDecidedEvent(
        Long loanId,
        Long farmerUserId,
        boolean approved,
        BigDecimal amountApproved,
        String remarks,
        String schemeName
) {
}
 
































