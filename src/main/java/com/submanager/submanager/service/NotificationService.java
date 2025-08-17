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

import java.time.LocalDate;
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

    public NotificationDto createRenewalReminder(Long accountId, Long subscriptionId,
                                                 String title, String message, LocalDate dueDate) {
        var acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
        Subscription sub = null;
        if (subscriptionId != null) {
            sub = subscriptionRepo.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("subscription no encontrada"));
            // Evita duplicados del mismo recordatorio
            if (dueDate != null && repo.existsBySubscription_IdAndTypeAndDueDate(subscriptionId,
                    NotificationType.RENEWAL_REMINDER, dueDate)) {
                // ya existe, devuelve el existente? para simplificar, retorna null y que el caller ignore
                return null;
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
