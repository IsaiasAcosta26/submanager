package com.submanager.submanager.dto.record;

import com.submanager.submanager.model.enums.PaymentStatus;
import com.submanager.submanager.model.enums.BillingCycle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentDto(
        Long id,
        Long accountId,
        Long subscriptionId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime paidAt,
        LocalDate periodStart,
        LocalDate periodEnd,
        BillingCycle billingCycle,
        String provider,
        String providerRef,
        String message
) {
}
