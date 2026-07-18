package com.agrofinance.event;
 
import com.agrofinance.entity.NotificationType;
import com.agrofinance.repository.FarmerRepository;
import com.agrofinance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
 
/**
 * The ONLY place events become notifications. Business services publish
 * facts ("a loan was decided") without knowing this class exists — add
 * email/SMS later by adding listeners, touching zero business code.
 *
 * AFTER_COMMIT: fires only if the originating transaction actually
 * committed — no "loan approved!" notification for a rolled-back
 * approval. REQUIRES_NEW: the original transaction is finished by then,
 * so our own writes need a fresh one or they silently don't flush.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
 
    private final NotificationService notificationService;
    private final FarmerRepository farmerRepository;
 
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserRegistered(UserRegisteredEvent event) {
        notificationService.notify(
                event.userId(),
                "Welcome to AgroFinance!",
                "Your account was created successfully. Complete your profile to get started.",
                NotificationType.SYSTEM
        );
    }
 
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLoanDecided(LoanDecidedEvent event) {
        if (event.approved()) {
            notificationService.notify(
                    event.farmerUserId(),
                    "Loan approved 🎉",
                    "Your application under '" + event.schemeName() + "' was approved for ₹"
                            + event.amountApproved() + ". Disbursement will follow.",
                    NotificationType.LOAN_UPDATE
            );
        } else {
            notificationService.notify(
                    event.farmerUserId(),
                    "Loan application update",
                    "Your application under '" + event.schemeName() + "' was not approved. Officer remarks: "
                            + event.remarks(),
                    NotificationType.LOAN_UPDATE
            );
        }
    }
 
    /**
     * Fan-out: one event -> one notification per farmer. Fine at our
     * scale; at thousands of farmers this becomes a batch/async job —
     * worth knowing this is where that pressure would appear first.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSchemeCreated(SchemeCreatedEvent event) {
        farmerRepository.findAll().forEach(farmer ->
                notificationService.notify(
                        farmer.getUserId(),
                        "New loan scheme available",
                        "'" + event.schemeName() + "' is now open for applications. "
                                + (event.description() != null ? event.description() : ""),
                        NotificationType.SYSTEM
                )
        );
    }
 
}
 
































