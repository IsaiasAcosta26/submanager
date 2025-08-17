package com.submanager.submanager.controller;

import com.submanager.submanager.dto.record.DashboardDto;
import com.submanager.submanager.service.DashboardService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/{accountId}")
    public DashboardDto get(
            @PathVariable Long accountId,
            @RequestParam(required = false) @Min(1) Integer days,
            @RequestParam(required = false) @Min(1) Integer notificationsLimit,
            @RequestParam(required = false) @Min(1) Integer topCategories
    ) {
        return service.get(accountId, days, notificationsLimit, topCategories);
    }
}
