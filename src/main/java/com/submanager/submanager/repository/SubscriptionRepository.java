package com.submanager.submanager.repository;

import com.submanager.submanager.model.entity.Subscription;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends
        JpaRepository<Subscription, Long>,
        JpaSpecificationExecutor<Subscription> {

    List<Subscription> findByAccount_Id(Long accountId);

    @Query("""
           select s
           from Subscription s
           where s.account.id = :accountId
             and s.nextRenewalDate is not null
             and s.nextRenewalDate <= :until
             and s.status = :status
           order by s.nextRenewalDate asc
           """)
    List<Subscription> findUpcomingRenewals(Long accountId, LocalDate until, SubscriptionStatus status);

    // Helper para llamadas “por defecto” (ACTIVE) sin duplicar lógica en servicios
    default List<Subscription> findUpcomingRenewalsActive(Long accountId, LocalDate until) {
        return findUpcomingRenewals(accountId, until, SubscriptionStatus.ACTIVE);
    }
}

