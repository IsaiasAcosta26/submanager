package com.submanager.submanager.model.entity;

import com.submanager.submanager.model.enums.NotificationStatus;
import com.submanager.submanager.model.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notif_account", columnList = "account_id"),
                @Index(name = "idx_notif_due", columnList = "dueDate")
        },
        uniqueConstraints = {
                // Evita duplicados del mismo recordatorio por suscripción+fecha+tipo
                @UniqueConstraint(name = "uk_notif_sub_type_due",
                        columnNames = {"subscription_id", "type", "dueDate"})
        })
@Getter @Setter
public class Notification extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription; // opcional, pero útil para abrir detalle

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private NotificationType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private NotificationStatus status = NotificationStatus.PENDING;

    @NotBlank
    @Column(nullable = false, length = 140)
    private String title;

    @Column(length = 500)
    private String message;

    private LocalDate dueDate;  // fecha relevante (ej. próxima renovación)
}
