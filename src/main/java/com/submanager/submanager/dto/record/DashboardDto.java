package com.submanager.submanager.dto.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DashboardDto(
        BigDecimal monthlyTotal,
        List<CategorySlice> topCategories,
        List<UpcomingItem> upcomingRenewals,
        List<NotificationItem> notifications
) {
    public record CategorySlice(Long categoryId, String categoryName, BigDecimal totalMonthly) {}
    public record UpcomingItem(
            Long id,
            String name,
            String provider,
            LocalDate nextRenewalDate,
            BigDecimal price,
            String currency,
            String billingCycle,
            String status
    ) {}
    public record NotificationItem(
            Long id,
            String type,
            String title,
            String message,
            LocalDate dueDate,
            Instant createdAt
    ) {}
}
