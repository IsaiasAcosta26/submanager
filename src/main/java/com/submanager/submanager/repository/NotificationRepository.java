package com.submanager.submanager.repository;

import com.submanager.submanager.model.entity.Notification;
import com.submanager.submanager.model.enums.NotificationStatus;
import com.submanager.submanager.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Listado simple (ya lo tenías)
    List<Notification> findByAccount_IdAndStatusOrderByCreatedAtDesc(Long accountId, NotificationStatus status);

    // Listado con límite (paginado)
    Page<Notification> findByAccount_IdAndStatus(Long accountId, NotificationStatus status, Pageable pageable);

    // Evitar duplicados por tipo + fecha
    boolean existsBySubscription_IdAndTypeAndDueDate(Long subscriptionId,
                                                     NotificationType type,
                                                     LocalDate dueDate);

    // Contador de no leídas
    long countByAccount_IdAndStatus(Long accountId, NotificationStatus status);

    // Bulk: marcar todas por cuenta de source->target (ej: PENDING -> READ)
    @Modifying
    @Query("update Notification n set n.status = :target where n.account.id = :accountId and n.status = :source")
    int bulkUpdateStatus(Long accountId, NotificationStatus source, NotificationStatus target);

    // Bulk: borrar por estado
    @Modifying
    @Query("delete from Notification n where n.account.id = :accountId and n.status = :status")
    int bulkDeleteByStatus(Long accountId, NotificationStatus status);

    // Bulk: marcar por IDs
    @Modifying
    @Query("update Notification n set n.status = :target where n.id in :ids")
    int bulkUpdateStatusByIds(Collection<Long> ids, NotificationStatus target);
}
