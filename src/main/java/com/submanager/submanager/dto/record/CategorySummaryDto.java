package com.submanager.submanager.dto.record;

import java.math.BigDecimal;

public record CategorySummaryDto(
        Long categoryId,
        String categoryName,
        BigDecimal totalMonthly
) {}
