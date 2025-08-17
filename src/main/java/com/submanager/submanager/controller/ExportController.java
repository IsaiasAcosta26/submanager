package com.submanager.submanager.controller;

import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.service.ExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exports")
public class ExportController {

    private final ExportService service;

    public ExportController(ExportService service) {
        this.service = service;
    }

    // ---------- CSV ----------
    @GetMapping(value = "/subscriptions.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam Long accountId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String nameContains,
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) BillingCycle billingCycle,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate renewalFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate renewalTo,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(defaultValue = "any") String tagsMode, // any | all
            @RequestParam(defaultValue = "nextRenewalDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) throws Exception {
        var list = service.findForExport(accountId, provider, nameContains, status, billingCycle,
                minPrice, maxPrice, renewalFrom, renewalTo, categoryId, tagIds, tagsMode, sortBy, direction);

        byte[] bytes = service.toCsv(list);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"subscriptions.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    // ---------- EXCEL ----------
    @GetMapping(value = "/subscriptions.xlsx",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam Long accountId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String nameContains,
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) BillingCycle billingCycle,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate renewalFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate renewalTo,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(defaultValue = "any") String tagsMode,
            @RequestParam(defaultValue = "nextRenewalDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) throws Exception {
        var list = service.findForExport(accountId, provider, nameContains, status, billingCycle,
                minPrice, maxPrice, renewalFrom, renewalTo, categoryId, tagIds, tagsMode, sortBy, direction);

        byte[] bytes = service.toExcel(list);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"subscriptions.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(bytes);
    }
}
