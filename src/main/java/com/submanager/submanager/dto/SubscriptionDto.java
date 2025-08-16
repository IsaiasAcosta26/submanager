package com.submanager.submanager.dto;

import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionDto(
        Long id,
        @NotNull Long accountId,
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Size(min = 2, max = 100) String provider,
        @Size(max = 80) String plan,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotBlank @Size(min = 3, max = 10) String currency,
        @NotNull BillingCycle billingCycle,
        LocalDate nextRenewalDate,
        SubscriptionStatus status,
        LocalDate lastActivityDate,
        @Size(max = 255) String notes
) {}
