package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.NotificationDto;
import com.submanager.submanager.mapper.NotificationMapper;
import com.submanager.submanager.model.entity.Notification;
import com.submanager.submanager.model.entity.Subscription;
import com.submanager.submanager.model.enums.NotificationStatus;
import com.submanager.submanager.model.enums.NotificationType;
import com.submanager.submanager.repository.AccountRepository;
import com.submanager.submanager.repository.NotificationRepository;
import com.submanager.submanager.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository repo;
    private final AccountRepository accountRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final NotificationMapper mapper;

    public NotificationService(NotificationRepository repo,
                               AccountRepository accountRepo,
                               SubscriptionRepository subscriptionRepo,
                               NotificationMapper mapper) {
        this.repo = repo;
        this.accountRepo = accountRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.mapper = mapper;
    }

    // ------------------ CREACIÓN ------------------

    public NotificationDto createRenewalReminder(Long accountId, Long subscriptionId,
                                                 String title, String message, LocalDate dueDate) {
        var acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
        Subscription sub = null;
        if (subscriptionId != null) {
            sub = subscriptionRepo.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("subscription no encontrada"));
            if (dueDate != null && repo.existsBySubscription_IdAndTypeAndDueDate(subscriptionId,
                    NotificationType.RENEWAL_REMINDER, dueDate)) {
                return null; // ya existe para esa fecha (evita duplicados)
            }
        }

        var n = new Notification();
        n.setAccount(acc);
        n.setSubscription(sub);
        n.setType(NotificationType.RENEWAL_REMINDER);
        n.setTitle(title);
        n.setMessage(message);
        n.setDueDate(dueDate);
        repo.save(n);
        return mapper.toDto(n);
    }

    public NotificationDto createSavingSuggestion(Long accountId, Long subscriptionId,
                                                  String title, String message) {
        var acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
        Subscription sub = null;
        var today = LocalDate.now();
        if (subscriptionId != null) {
            sub = subscriptionRepo.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("subscription no encontrada"));
            if (repo.existsBySubscription_IdAndTypeAndDueDate(subscriptionId,
                    NotificationType.SAVING_SUGGESTION, today)) {
                return null; // una por día
            }
        }

        var n = new Notification();
        n.setAccount(acc);
        n.setSubscription(sub);
        n.setType(NotificationType.SAVING_SUGGESTION);
        n.setTitle(title);
        n.setMessage(message);
        n.setDueDate(today);
        repo.save(n);
        return mapper.toDto(n);
    }

    // === NUEVO: Pago recibido ===
    public NotificationDto createPaymentReceived(Long accountId, Long subscriptionId,
                                                 BigDecimal amount, String currency,
                                                 LocalDateTime paidAt,
                                                 LocalDate periodStart, LocalDate periodEnd) {
        var acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
        var sub = subscriptionRepo.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("subscription no encontrada"));

        LocalDate day = (paidAt != null ? paidAt.toLocalDate() : LocalDate.now());
        // evita duplicar la misma notificación el mismo día para esa suscripción
        if (repo.existsBySubscription_IdAndTypeAndDueDate(subscriptionId, NotificationType.PAYMENT_RECEIVED, day)) {
            return null;
        }

        String title = "Pago recibido: " + (sub.getName() != null ? sub.getName() : sub.getProvider());
        StringBuilder msg = new StringBuilder("Se registró un pago de ");
        msg.append(amount != null ? amount : BigDecimal.ZERO)
                .append(" ")
                .append(currency != null ? currency : "");
        if (periodStart != null && periodEnd != null) {
            msg.append(". Cubre del ").append(periodStart).append(" al ").append(periodEnd);
        }

        var n = new Notification();
        n.setAccount(acc);
        n.setSubscription(sub);
        n.setType(NotificationType.PAYMENT_RECEIVED);
        n.setTitle(title);
        n.setMessage(msg.toString());
        n.setDueDate(day); // usamos el día del pago para deduplicar
        repo.save(n);
        return mapper.toDto(n);
    }

    // === NUEVO: Pago fallido (opcional) ===
    public NotificationDto createPaymentFailed(Long accountId, Long subscriptionId,
                                               BigDecimal amount, String currency,
                                               LocalDateTime paidAt, String errorMsg) {
        var acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
        var sub = subscriptionRepo.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("subscription no encontrada"));

        LocalDate day = (paidAt != null ? paidAt.toLocalDate() : LocalDate.now());
        if (repo.existsBySubscription_IdAndTypeAndDueDate(subscriptionId, NotificationType.PAYMENT_FAILED, day)) {
            return null;
        }

        String title = "Pago fallido: " + (sub.getName() != null ? sub.getName() : sub.getProvider());
        String msg = (errorMsg != null && !errorMsg.isBlank())
                ? errorMsg
                : ("Monto " + (amount != null ? amount : BigDecimal.ZERO) + " " + (currency != null ? currency : ""));

        var n = new Notification();
        n.setAccount(acc);
        n.setSubscription(sub);
        n.setType(NotificationType.PAYMENT_FAILED);
        n.setTitle(title);
        n.setMessage(msg);
        n.setDueDate(day);
        repo.save(n);
        return mapper.toDto(n);
    }

    // ------------------ CONSULTA / ESTADO ------------------

    @Transactional(readOnly = true)
    public List<NotificationDto> list(Long accountId, NotificationStatus status) {
        var st = (status != null) ? status : NotificationStatus.PENDING;
        return repo.findByAccount_IdAndStatusOrderByCreatedAtDesc(accountId, st)
                .stream().map(mapper::toDto).toList();
    }

    public void markAsRead(Long id) {
        var n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("notification no encontrada"));
        n.setStatus(NotificationStatus.READ);
    }
}
