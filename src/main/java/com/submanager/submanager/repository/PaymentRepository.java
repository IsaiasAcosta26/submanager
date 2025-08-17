package com.submanager.submanager.repository;

import com.submanager.submanager.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySubscription_IdOrderByPaidAtDesc(Long subscriptionId);
}
