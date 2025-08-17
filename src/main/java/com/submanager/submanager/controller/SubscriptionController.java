package com.submanager.submanager.controller;

import com.submanager.submanager.common.PageResponse;
import com.submanager.submanager.dto.record.SubscriptionDto;
import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;
    public SubscriptionController(SubscriptionService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto create(@RequestBody @Valid SubscriptionDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public SubscriptionDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public SubscriptionDto update(@PathVariable Long id, @RequestBody @Valid SubscriptionDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // -------- LISTADO PAGINADO + FILTROS --------
    @GetMapping
    public PageResponse<SubscriptionDto> search(
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
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "nextRenewalDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return service.search(
                accountId, provider, nameContains, status, billingCycle,
                minPrice, maxPrice, renewalFrom, renewalTo,
                categoryId, tagIds, tagsMode,
                page, size, sortBy, direction
        );
    }

    // -------- INSIGHTS --------
    @GetMapping("/insights/{accountId}/monthly-total")
    public BigDecimal monthlyTotal(@PathVariable Long accountId) {
        return service.monthlyTotal(accountId);
    }

    // **ÚNICO** endpoint para upcoming: status es opcional
    @GetMapping("/insights/{accountId}/upcoming")
    public List<SubscriptionDto> upcoming(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "14") @Min(1) int days,
            @RequestParam(required = false) SubscriptionStatus status
    ) {
        return service.upcomingRenewals(accountId, days, status);
    }

    @GetMapping("/insights/{accountId}/suggestions")
    public List<String> suggestions(@PathVariable Long accountId) {
        return service.simpleSuggestions(accountId);
    }
}
