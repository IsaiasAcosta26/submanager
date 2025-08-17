package com.submanager.submanager.repository;

import com.submanager.submanager.model.entity.Notification;
import com.submanager.submanager.model.enums.NotificationStatus;
import com.submanager.submanager.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByAccount_IdAndStatusOrderByCreatedAtDesc(Long accountId, NotificationStatus status);

    boolean existsBySubscription_IdAndTypeAndDueDate(Long subscriptionId,
                                                     NotificationType type,
                                                     LocalDate dueDate);
}
