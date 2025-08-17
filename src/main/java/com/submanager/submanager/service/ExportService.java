package com.submanager.submanager.service;

import com.submanager.submanager.model.entity.Subscription;
import com.submanager.submanager.model.entity.Tag;
import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.repository.SubscriptionRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExportService {

    private final SubscriptionRepository repo;

    public ExportService(SubscriptionRepository repo) {
        this.repo = repo;
    }

    // ---------- BÚSQUEDA (sin paginar) ----------
    public List<Subscription> findForExport(
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
            String sortBy,
            String direction
    ) {
        if (accountId == null) throw new IllegalArgumentException("accountId es requerido");

        Specification<Subscription> spec = (r, q, cb) -> cb.equal(r.get("account").get("id"), accountId);

        if (provider != null && !provider.isBlank())
            spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("provider")), "%" + provider.toLowerCase() + "%"));

        if (nameContains != null && !nameContains.isBlank())
            spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("name")), "%" + nameContains.toLowerCase() + "%"));

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

        return repo.findAll(spec, sort);
    }

    private Specification<Subscription> hasAnyTag(List<Long> tagIds) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Subscription, Tag> jt = root.join("tags");
            return jt.get("id").in(tagIds);
        };
    }

    private Specification<Subscription> hasAllTags(List<Long> tagIds) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> preds = new ArrayList<>();
            for (Long tagId : tagIds) preds.add(existsTag(root, query, cb, tagId));
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
                .where(cb.equal(s2.get("id"), root.get("id")), cb.equal(jt.get("id"), tagId));
        return cb.exists(sq);
    }

    // ---------- CSV ----------
    public byte[] toCsv(List<Subscription> list) throws Exception {
        String[] headers = {
                "id", "accountId", "name", "provider", "plan",
                "price", "currency", "billingCycle", "monthlyEquivalent",
                "nextRenewalDate", "status", "lastActivityDate", "notes",
                "categoryName", "tagNames"
        };

        try (var baos = new ByteArrayOutputStream();
             var writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             var csv = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            for (var s : list) {
                String categoryName = (s.getCategory() != null) ? s.getCategory().getName() : "";
                String tagNames = (s.getTags() == null || s.getTags().isEmpty())
                        ? ""
                        : s.getTags().stream().map(Tag::getName).sorted().reduce((a, b) -> a + ";" + b).orElse("");

                csv.printRecord(
                        s.getId(),
                        s.getAccount() != null ? s.getAccount().getId() : null,
                        s.getName(),
                        s.getProvider(),
                        s.getPlan(),
                        s.getPrice(),
                        s.getCurrency(),
                        s.getBillingCycle(),
                        monthlyEquivalent(s.getPrice(), s.getBillingCycle()),
                        s.getNextRenewalDate(),
                        s.getStatus(),
                        s.getLastActivityDate(),
                        s.getNotes(),
                        categoryName,
                        tagNames
                );
            }
            csv.flush();
            return baos.toByteArray();
        }
    }

    // ---------- EXCEL (.xlsx) ----------
    public byte[] toExcel(List<Subscription> list) throws Exception {
        // Evita si no tienes POI en dependencias
        try (var baos = new ByteArrayOutputStream();
             var wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            var sheet = wb.createSheet("subscriptions");
            int r = 0;

            String[] headers = {
                    "ID", "AccountId", "Name", "Provider", "Plan",
                    "Price", "Currency", "BillingCycle", "MonthlyEquivalent",
                    "NextRenewalDate", "Status", "LastActivityDate", "Notes",
                    "CategoryName", "TagNames"
            };

            var header = sheet.createRow(r++);
            for (int c = 0; c < headers.length; c++) header.createCell(c).setCellValue(headers[c]);

            for (var s : list) {
                String categoryName = (s.getCategory() != null) ? s.getCategory().getName() : "";
                String tagNames = (s.getTags() == null || s.getTags().isEmpty())
                        ? ""
                        : s.getTags().stream().map(Tag::getName).sorted().reduce((a, b) -> a + ";" + b).orElse("");

                var row = sheet.createRow(r++);
                int c = 0;
                row.createCell(c++).setCellValue(s.getId() != null ? s.getId() : 0);
                row.createCell(c++).setCellValue(s.getAccount() != null && s.getAccount().getId() != null ? s.getAccount().getId() : 0);
                row.createCell(c++).setCellValue(nz(s.getName()));
                row.createCell(c++).setCellValue(nz(s.getProvider()));
                row.createCell(c++).setCellValue(nz(s.getPlan()));
                row.createCell(c++).setCellValue(s.getPrice() != null ? s.getPrice().doubleValue() : 0d);
                row.createCell(c++).setCellValue(nz(s.getCurrency()));
                row.createCell(c++).setCellValue(s.getBillingCycle() != null ? s.getBillingCycle().name() : "");
                row.createCell(c++).setCellValue(monthlyEquivalent(s.getPrice(), s.getBillingCycle()).doubleValue());
                row.createCell(c++).setCellValue(s.getNextRenewalDate() != null ? s.getNextRenewalDate().toString() : "");
                row.createCell(c++).setCellValue(s.getStatus() != null ? s.getStatus().name() : "");
                row.createCell(c++).setCellValue(s.getLastActivityDate() != null ? s.getLastActivityDate().toString() : "");
                row.createCell(c++).setCellValue(nz(s.getNotes()));
                row.createCell(c++).setCellValue(categoryName);
                row.createCell(c++).setCellValue(tagNames);
            }

            for (int c = 0; c < headers.length; c++) sheet.autoSizeColumn(c);

            wb.write(baos);
            return baos.toByteArray();
        }
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private BigDecimal monthlyEquivalent(BigDecimal price, BillingCycle cycle) {
        if (price == null || cycle == null) return BigDecimal.ZERO;
        return switch (cycle) {
            case MONTHLY -> price;
            case YEARLY -> price.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
            case WEEKLY -> price.multiply(BigDecimal.valueOf(4.345));
        };
    }
}
