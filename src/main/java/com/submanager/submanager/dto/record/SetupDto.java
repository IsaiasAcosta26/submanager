package com.submanager.submanager.dto.record;

import java.math.BigDecimal;
import java.util.List;

public record SetupDto(
        AccountInfo account,
        Settings settings,
        List<CategoryItem> categories,
        List<TagItem> tags,
        DashboardDto dashboard
) {
    public record AccountInfo(Long id, String name, String email) {}
    public record Settings(
            int remindersDaysAhead,
            BigDecimal savingsHighCost,
            int savingsInactiveDays,
            int dashboardUpcomingDays,
            int dashboardNotificationsLimit,
            int dashboardTopCategories
    ) {}
    public record CategoryItem(Long id, String name, String color, String description) {}
    public record TagItem(Long id, String name, String description) {}
}
