package com.submanager.submanager.service;

import com.submanager.submanager.common.PageResponse;
import com.submanager.submanager.dto.SubscriptionDto;
import com.submanager.submanager.model.entity.Subscription;
import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.repository.AccountRepository;
import com.submanager.submanager.repository.SubscriptionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository repo;
    private final AccountRepository accountRepo;

    public SubscriptionService(SubscriptionRepository repo, AccountRepository accountRepo) {
        this.repo = repo;
        this.accountRepo = accountRepo;
    }

    public SubscriptionDto create(SubscriptionDto dto) {
        var acc = accountRepo.findById(dto.accountId())
                .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));

        var s = new Subscription();
        s.setAccount(acc);
        s.setName(dto.name());
        s.setProvider(dto.provider());
        s.setPlan(dto.plan());
        s.setPrice(dto.price());
        s.setCurrency(dto.currency());
        s.setBillingCycle(dto.billingCycle());
        s.setNextRenewalDate(dto.nextRenewalDate());
        s.setStatus(dto.status() != null ? dto.status() : SubscriptionStatus.ACTIVE);
        s.setLastActivityDate(dto.lastActivityDate());
        s.setNotes(dto.notes());

        repo.save(s);
        return toDto(s);
    }

    @Transactional(readOnly = true)
    public SubscriptionDto get(Long id) {
        var s = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("subscription no encontrada"));
        return toDto(s);
    }

    public SubscriptionDto update(Long id, SubscriptionDto dto) {
        var s = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("subscription no encontrada"));
        if (!s.getAccount().getId().equals(dto.accountId())) {
            var acc = accountRepo.findById(dto.accountId())
                    .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
            s.setAccount(acc);
        }
        s.setName(dto.name());
        s.setProvider(dto.provider());
        s.setPlan(dto.plan());
        s.setPrice(dto.price());
        s.setCurrency(dto.currency());
        s.setBillingCycle(dto.billingCycle());
        s.setNextRenewalDate(dto.nextRenewalDate());
        s.setStatus(dto.status() != null ? dto.status() : SubscriptionStatus.ACTIVE);
        s.setLastActivityDate(dto.lastActivityDate());
        s.setNotes(dto.notes());
        return toDto(s);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("subscription no encontrada");
        repo.deleteById(id);
    }

    // ---------- SEARCH (paginado + filtros) ----------
    @Transactional(readOnly = true)
    public PageResponse<SubscriptionDto> search(
            Long accountId,
            String provider,
            String nameContains,
            SubscriptionStatus status,
            BillingCycle billingCycle,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate renewalFrom,
            LocalDate renewalTo,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        if (accountId == null) throw new IllegalArgumentException("accountId es requerido");

        Specification<Subscription> spec = byAccount(accountId);

        if (provider != null && !provider.isBlank())
            spec = spec.and(likeLower("provider", provider));

        if (nameContains != null && !nameContains.isBlank())
            spec = spec.and(likeLower("name", nameContains));

        if (status != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), status));

        if (billingCycle != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("billingCycle"), billingCycle));

        if (minPrice != null)
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("price"), minPrice));

        if (maxPrice != null)
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("price"), maxPrice));

        if (renewalFrom != null)
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("nextRenewalDate"), renewalFrom));

        if (renewalTo != null)
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("nextRenewalDate"), renewalTo));

        Sort sort = Sort.by((sortBy == null || sortBy.isBlank()) ? "nextRenewalDate" : sortBy);
        if ("desc".equalsIgnoreCase(direction)) sort = sort.descending();

        var p = repo.findAll(spec, PageRequest.of(page, size, sort));
        var items = p.getContent().stream().map(this::toDto).toList();
        return new PageResponse<>(items, p.getTotalElements(), p.getNumber(), p.getSize());
    }

    private Specification<Subscription> byAccount(Long accountId) {
        return (r, q, cb) -> cb.equal(r.get("account").get("id"), accountId);
    }

    private Specification<Subscription> likeLower(String field, String value) {
        return (r, q, cb) -> cb.like(cb.lower(r.get(field)), "%" + value.toLowerCase() + "%");
    }

    // ---------- INSIGHTS ----------
    @Transactional(readOnly = true)
    public java.math.BigDecimal monthlyTotal(Long accountId) {
        var list = repo.findByAccount_Id(accountId);
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (var s : list) {
            total = total.add(monthlyEquivalent(s.getPrice(), s.getBillingCycle()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> upcomingRenewals(Long accountId, int days) {
        var until = LocalDate.now().plusDays(days);
        return repo.findUpcomingRenewals(accountId, until, SubscriptionStatus.ACTIVE).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> simpleSuggestions(Long accountId) {
        var list = repo.findByAccount_Id(accountId);
        return list.stream().map(s -> {
            boolean longTimeNoUse = s.getLastActivityDate() != null
                    && s.getLastActivityDate().isBefore(LocalDate.now().minusDays(60));
            boolean nearRenewal = s.getNextRenewalDate() != null
                    && !s.getNextRenewalDate().isAfter(LocalDate.now().plusDays(7));
            var monthly = monthlyEquivalent(s.getPrice(), s.getBillingCycle());

            if (longTimeNoUse && nearRenewal) {
                return "Revisar '" + s.getName() + "' (" + s.getProvider()
                        + "): sin uso >60 días y renueva pronto. Ahorro mensual "
                        + monthly + " " + s.getCurrency();
            } else if (longTimeNoUse) {
                return "Revisar '" + s.getName() + "': sin uso >60 días. Considera pausar.";
            } else if (nearRenewal && monthly.compareTo(java.math.BigDecimal.valueOf(20)) > 0) {
                return "Renueva pronto '" + s.getName() + "' con costo mensual ~"
                        + monthly + " " + s.getCurrency() + ". ¿Bajar de plan?";
            }
            return null;
        }).filter(m -> m != null).toList();
    }

    // --------- helpers ---------
    private java.math.BigDecimal monthlyEquivalent(java.math.BigDecimal price, BillingCycle cycle) {
        if (price == null || cycle == null) return java.math.BigDecimal.ZERO;
        return switch (cycle) {
            case MONTHLY -> price;
            case YEARLY -> price.divide(java.math.BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
            case WEEKLY -> price.multiply(java.math.BigDecimal.valueOf(4.345));
        };
    }

    private SubscriptionDto toDto(Subscription s) {
        return new SubscriptionDto(
                s.getId(),
                s.getAccount().getId(),
                s.getName(),
                s.getProvider(),
                s.getPlan(),
                s.getPrice(),
                s.getCurrency(),
                s.getBillingCycle(),
                s.getNextRenewalDate(),
                s.getStatus(),
                s.getLastActivityDate(),
                s.getNotes()
        );
    }
}
