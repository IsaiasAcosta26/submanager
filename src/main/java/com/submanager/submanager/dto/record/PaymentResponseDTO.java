package com.submanager.submanager.dto.record;

import com.submanager.submanager.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        Long paymentId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime paidAt,
        Long subscriptionId,
        LocalDate oldNextRenewalDate,
        LocalDate newNextRenewalDate,
        String message
) {}
