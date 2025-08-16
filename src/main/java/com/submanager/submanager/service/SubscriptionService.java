package com.submanager.submanager.service;

import com.submanager.submanager.common.PageResponse;
import com.submanager.submanager.dto.record.SubscriptionDto;
import com.submanager.submanager.model.entity.Category;
import com.submanager.submanager.model.entity.Subscription;
import com.submanager.submanager.model.entity.Tag;
import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.repository.AccountRepository;
import com.submanager.submanager.repository.CategoryRepository;
import com.submanager.submanager.repository.SubscriptionRepository;
import com.submanager.submanager.repository.TagRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository repo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;

    public SubscriptionService(SubscriptionRepository repo,
                               AccountRepository accountRepo,
                               CategoryRepository categoryRepo,
                               TagRepository tagRepo) {
        this.repo = repo;
        this.accountRepo = accountRepo;
        this.categoryRepo = categoryRepo;
        this.tagRepo = tagRepo;
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

        // Asociaciones opcionales
        if (dto.categoryId() != null) {
            Category c = categoryRepo.findById(dto.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("category no encontrada"));
            s.setCategory(c);
        }
        if (dto.tagIds() != null) {
            Set<Tag> tags = toTags(dto.tagIds());
            s.setTags(tags);
        }

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

        // Reemplazo completo de asociaciones si vienen en el DTO
        if (dto.categoryId() != null) {
            Category c = categoryRepo.findById(dto.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("category no encontrada"));
            s.setCategory(c);
        } else {
            s.setCategory(null);
        }
        if (dto.tagIds() != null) {
            s.setTags(toTags(dto.tagIds()));
        }

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
            Long categoryId,
            List<Long> tagIds,
            String tagsMode, // "any" | "all"
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

        if (categoryId != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("category").get("id"), categoryId));

        if (tagIds != null && !tagIds.isEmpty()) {
            if ("all".equalsIgnoreCase(tagsMode)) {
                spec = spec.and(hasAllTags(tagIds));
            } else {
                spec = spec.and(hasAnyTag(tagIds));
            }
        }

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

    private Specification<Subscription> hasAnyTag(List<Long> tagIds) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Subscription, Tag> jt = root.join("tags");
            return jt.get("id").in(tagIds);
        };
    }

    // Para "ALL": existe una fila por cada tagId asociada a la misma suscripción
    private Specification<Subscription> hasAllTags(List<Long> tagIds) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> preds = new ArrayList<>();
            for (Long tagId : tagIds) {
                preds.add(existsTag(root, query, cb, tagId));
            }
            query.distinct(true);
            return cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private jakarta.persistence.criteria.Predicate existsTag(Root<Subscription> root,
                                                             jakarta.persistence.criteria.CriteriaQuery<?> query,
                                                             jakarta.persistence.criteria.CriteriaBuilder cb,
                                                             Long tagId) {
        Subquery<Long> sq = query.subquery(Long.class);
        Root<Subscription> s2 = sq.from(Subscription.class);
        Join<Subscription, Tag> jt = s2.join("tags");
        sq.select(s2.get("id"))
                .where(
                        cb.equal(s2.get("id"), root.get("id")),
                        cb.equal(jt.get("id"), tagId)
                );
        return cb.exists(sq);
    }

    // ---------- INSIGHTS ----------
    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(Long accountId) {
        var list = repo.findByAccount_Id(accountId);
        BigDecimal total = BigDecimal.ZERO;
        for (var s : list) {
            total = total.add(monthlyEquivalent(s.getPrice(), s.getBillingCycle()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> upcomingRenewals(Long accountId, int days) {
        return upcomingRenewals(accountId, days, SubscriptionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> upcomingRenewals(Long accountId, int days, SubscriptionStatus status) {
        var until = LocalDate.now().plusDays(days);
        var st = (status != null ? status : SubscriptionStatus.ACTIVE);
        return repo.findUpcomingRenewals(accountId, until, st).stream()
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
            } else if (nearRenewal && monthly.compareTo(BigDecimal.valueOf(20)) > 0) {
                return "Renueva pronto '" + s.getName() + "' con costo mensual ~"
                        + monthly + " " + s.getCurrency() + ". ¿Bajar de plan?";
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    // --------- helpers ---------
    private BigDecimal monthlyEquivalent(BigDecimal price, BillingCycle cycle) {
        if (price == null || cycle == null) return BigDecimal.ZERO;
        return switch (cycle) {
            case MONTHLY -> price;
            case YEARLY -> price.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
            case WEEKLY -> price.multiply(BigDecimal.valueOf(4.345));
        };
    }

    private Set<Tag> toTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return new HashSet<>();
        var found = tagRepo.findAllById(tagIds);
        if (found.size() != new HashSet<>(tagIds).size()) {
            throw new IllegalArgumentException("algunos tagIds no existen");
        }
        return new HashSet<>(found);
    }

    private SubscriptionDto toDto(Subscription s) {
        Long categoryId = (s.getCategory() != null) ? s.getCategory().getId() : null;
        List<Long> tagIds = (s.getTags() != null)
                ? s.getTags().stream().map(Tag::getId).toList()
                : List.of();

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
                s.getNotes(),
                categoryId,
                tagIds
        );
    }
}
