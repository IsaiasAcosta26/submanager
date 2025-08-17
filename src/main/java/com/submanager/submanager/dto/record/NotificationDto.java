package com.submanager.submanager.dto.record;

import com.submanager.submanager.model.enums.NotificationStatus;
import com.submanager.submanager.model.enums.NotificationType;

import java.time.LocalDate;

public record NotificationDto(
        Long id,
        Long accountId,
        Long subscriptionId,
        NotificationType type,
        NotificationStatus status,
        String title,
        String message,
        LocalDate dueDate,
        java.time.Instant createdAt
) {}
