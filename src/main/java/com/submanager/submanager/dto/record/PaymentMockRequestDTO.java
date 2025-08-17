package com.submanager.submanager.dto.record;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentMockRequestDTO(
        Long accountId,
        Long subscriptionId,
        Boolean success,
        BigDecimal amount,
        String currency,
        LocalDateTime paidAt,
        String provider,
        String providerRef
) {
}
