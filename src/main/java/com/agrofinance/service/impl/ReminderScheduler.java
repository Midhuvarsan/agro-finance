package com.agrofinance.service.impl;
 
import com.agrofinance.entity.Loan;
import com.agrofinance.entity.LoanStatus;
import com.agrofinance.entity.NotificationType;
import com.agrofinance.repository.LoanRepository;
import com.agrofinance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDateTime;
import java.util.List;
 
/**
 * Time-driven notifications. cron = second minute hour day month weekday
 * — "0 0 9 * * *" = every day at 09:00 server time.
 * For TESTING, temporarily swap to "0 * * * * *" (every minute).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {
 
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;
 
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void remindStalePendingLoans() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        List<Loan> stale = loanRepository.findStaleInReview(
                List.of(LoanStatus.PENDING, LoanStatus.AI_REVIEWED), cutoff);
 
        log.info("Reminder job: {} loan(s) in review for 3+ days", stale.size());
 
        stale.forEach(loan -> notificationService.notify(
                loan.getFarmer().getUserId(),
                "Application update",
                "Your loan application #" + loan.getId()
                        + " is still under review. We'll notify you as soon as there's a decision.",
                NotificationType.REMINDER
        ));
    }
 
}
 
































