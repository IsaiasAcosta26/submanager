package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.CategorySummaryDto;
import com.submanager.submanager.dto.record.DashboardDto;
import com.submanager.submanager.dto.record.NotificationDto;
import com.submanager.submanager.dto.record.SubscriptionDto;
import com.submanager.submanager.model.enums.NotificationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final SubscriptionService subscriptionService;
    private final AnalyticsService analyticsService;
    private final NotificationService notificationService;

    @Value("${dashboard.upcomingDays:7}")
    private int defUpcoming;

    @Value("${dashboard.notificationsLimit:5}")
    private int defNotifLimit;

    @Value("${dashboard.topCategories:5}")
    private int defTopCats;

    public DashboardService(SubscriptionService subscriptionService,
                            AnalyticsService analyticsService,
                            NotificationService notificationService) {
        this.subscriptionService = subscriptionService;
        this.analyticsService = analyticsService;
        this.notificationService = notificationService;
    }

    public DashboardDto get(Long accountId, Integer days, Integer notificationsLimit, Integer topCategories) {
        final int d  = (days == null || days < 1) ? defUpcoming     : days;
        final int nl = (notificationsLimit == null || notificationsLimit < 1) ? defNotifLimit : notificationsLimit;
        final int tc = (topCategories == null || topCategories < 1) ? defTopCats : topCategories;

        // 1) Total mensual normalizado (usa tu lógica existente)
        BigDecimal monthlyTotal = subscriptionService.monthlyTotal(accountId);

        // 2) Top categorías (ordenado desc por total)
        List<CategorySummaryDto> cat = analyticsService.monthlyByCategory(accountId).stream()
                .sorted(Comparator.comparing(CategorySummaryDto::totalMonthly).reversed())
                .limit(tc)
                .toList();

        var topSlices = cat.stream()
                .map(c -> new DashboardDto.CategorySlice(c.categoryId(), c.categoryName(), c.totalMonthly()))
                .toList();

        // 3) Renovaciones próximas (n días) – usa ACTIVE por defecto en tu service
        List<SubscriptionDto> upcoming = subscriptionService.upcomingRenewals(accountId, d);
        var upcomingItems = upcoming.stream()
                .map(s -> new DashboardDto.UpcomingItem(
                        s.id(),
                        s.name(),
                        s.provider(),
                        s.nextRenewalDate(),
                        s.price(),
                        s.currency(),
                        s.billingCycle() != null ? s.billingCycle().name() : null,
                        s.status() != null ? s.status().name() : null
                )).toList();

        // 4) Últimas notificaciones (PENDING), limitadas
        List<NotificationDto> notifs = notificationService.list(accountId, NotificationStatus.PENDING);
        var notifItems = notifs.stream().limit(nl)
                .map(n -> new DashboardDto.NotificationItem(
                        n.id(),
                        n.type() != null ? n.type().name() : null,
                        n.title(),
                        n.message(),
                        n.dueDate(),
                        n.createdAt()
                )).toList();

        return new DashboardDto(monthlyTotal, topSlices, upcomingItems, notifItems);
    }
}
