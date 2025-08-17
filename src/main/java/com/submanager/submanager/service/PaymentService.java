package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.PaymentDto;
import com.submanager.submanager.dto.record.PaymentMockRequestDTO;
import com.submanager.submanager.dto.record.PaymentResponseDTO;
import com.submanager.submanager.model.entity.Account;
import com.submanager.submanager.model.entity.Payment;
import com.submanager.submanager.model.entity.Subscription;
import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.PaymentStatus;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.repository.AccountRepository;
import com.submanager.submanager.repository.PaymentRepository;
import com.submanager.submanager.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final AccountRepository accountRepo;
    private final NotificationService notificationService;                        // ← NUEVO

    public PaymentService(PaymentRepository paymentRepo,
                          SubscriptionRepository subscriptionRepo,
                          AccountRepository accountRepo,
                          NotificationService notificationService) {              // ← NUEVO
        this.paymentRepo = paymentRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.accountRepo = accountRepo;
        this.notificationService = notificationService;                           // ← NUEVO
    }

    public PaymentResponseDTO mockCharge(PaymentMockRequestDTO req) {
        if (req == null) throw new IllegalArgumentException("request null");
        if (req.accountId() == null || req.subscriptionId() == null)
            throw new IllegalArgumentException("accountId y subscriptionId son requeridos");

        Account acc = accountRepo.findById(req.accountId())
                .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
        Subscription sub = subscriptionRepo.findById(req.subscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("subscription no encontrada"));

        if (!sub.getAccount().getId().equals(acc.getId()))
            throw new IllegalArgumentException("la suscripción no pertenece al account indicado");

        boolean success = req.success() == null || req.success();  // default true
        BigDecimal amount = (req.amount() != null) ? req.amount() : nz(sub.getPrice());
        String currency = (req.currency() != null) ? req.currency() : nz(sub.getCurrency());
        LocalDateTime paidAt = (req.paidAt() != null) ? req.paidAt() : LocalDateTime.now();
        String provider = (req.provider() != null) ? req.provider() : "mock";
        String providerRef = (req.providerRef() != null) ? req.providerRef() : ("mock_" + UUID.randomUUID());

        LocalDate oldNext = sub.getNextRenewalDate();

        Payment p = new Payment();
        p.setAccount(acc);
        p.setSubscription(sub);
        p.setStatus(success ? PaymentStatus.PAID : PaymentStatus.FAILED);
        p.setAmount(amount);
        p.setCurrency(currency);
        p.setPaidAt(paidAt);
        p.setBillingCycle(sub.getBillingCycle());
        p.setProvider(provider);
        p.setProviderRef(providerRef);
        p.setMessage(success ? "Pago simulado OK" : "Pago simulado FAILED");

        LocalDate newNext = oldNext;
        if (success) {
            LocalDate base = (oldNext != null) ? oldNext : paidAt.toLocalDate();
            newNext = advance(base, sub.getBillingCycle());
            p.setPeriodStart(base);
            p.setPeriodEnd(newNext.minusDays(1));

            sub.setNextRenewalDate(newNext);
            sub.setStatus(SubscriptionStatus.ACTIVE);
        }

        paymentRepo.save(p);

        // ---- NUEVO: emitir notificación según resultado ----
        if (success) {
            notificationService.createPaymentReceived(
                    acc.getId(), sub.getId(), p.getAmount(), p.getCurrency(),
                    p.getPaidAt(), p.getPeriodStart(), p.getPeriodEnd()
            );
        } else {
            notificationService.createPaymentFailed(
                    acc.getId(), sub.getId(), p.getAmount(), p.getCurrency(),
                    p.getPaidAt(), p.getMessage()
            );
        }

        return new PaymentResponseDTO(
                p.getId(),
                p.getStatus(),
                p.getAmount(),
                p.getCurrency(),
                p.getPaidAt(),
                sub.getId(),
                oldNext,
                newNext,
                p.getMessage()
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> listBySubscription(Long subscriptionId) {
        return paymentRepo.findBySubscription_IdOrderByPaidAtDesc(subscriptionId).stream()
                .map(p -> new PaymentDto(
                        p.getId(),
                        p.getAccount() != null ? p.getAccount().getId() : null,
                        p.getSubscription() != null ? p.getSubscription().getId() : null,
                        p.getStatus(),
                        p.getAmount(),
                        p.getCurrency(),
                        p.getPaidAt(),
                        p.getPeriodStart(),
                        p.getPeriodEnd(),
                        p.getBillingCycle(),
                        p.getProvider(),
                        p.getProviderRef(),
                        p.getMessage()
                ))
                .toList();
    }

    private LocalDate advance(LocalDate base, BillingCycle cycle) {
        if (cycle == null) cycle = BillingCycle.MONTHLY;
        return switch (cycle) {
            case MONTHLY -> base.plusMonths(1);
            case YEARLY  -> base.plusYears(1);
            case WEEKLY  -> base.plusWeeks(1);
        };
    }

    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private String nz(String v) { return v == null ? "" : v; }
}
