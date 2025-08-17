package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.CategorySummaryDto;
import com.submanager.submanager.model.entity.Subscription;
import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final SubscriptionRepository repo;

    public AnalyticsService(SubscriptionRepository repo) {
        this.repo = repo;
    }

    /**
     * Agrupa las suscripciones ACTIVAS del account por categoría
     * y suma su costo mensual equivalente (normaliza YEARLY/WEEKLY a mensual).
     */
    public List<CategorySummaryDto> monthlyByCategory(Long accountId) {
        var subs = repo.findByAccount_Id(accountId).stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();

        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, Long> nameToId = new HashMap<>();

        for (Subscription s : subs) {
            BigDecimal monthly = monthlyEquivalent(s.getPrice(), s.getBillingCycle());
            String name = (s.getCategory() != null) ? s.getCategory().getName() : "Sin categoría";
            Long catId = (s.getCategory() != null) ? s.getCategory().getId() : null;

            totals.merge(name, monthly, BigDecimal::add);
            nameToId.putIfAbsent(name, catId);
        }

        return totals.entrySet().stream()
                .map(e -> new CategorySummaryDto(
                        nameToId.get(e.getKey()),
                        e.getKey(),
                        e.getValue().setScale(2, RoundingMode.HALF_UP)
                ))
                .toList();
    }

    private BigDecimal monthlyEquivalent(BigDecimal price, BillingCycle cycle) {
        if (price == null || cycle == null) return BigDecimal.ZERO;
        return switch (cycle) {
            case MONTHLY -> price;
            case YEARLY -> price.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
            case WEEKLY -> price.multiply(BigDecimal.valueOf(4.345)); // semanas promedio por mes
        };
    }
}
