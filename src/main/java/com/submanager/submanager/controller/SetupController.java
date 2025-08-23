package com.submanager.submanager.controller;

import com.submanager.submanager.dto.record.SetupDto;
import com.submanager.submanager.service.SetupService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class SetupController {

    private final SetupService service;

    public SetupController(SetupService service) {
        this.service = service;
    }

    // GET /api/v1/me/setup?accountId=1&days=7&notificationsLimit=5&topCategories=5
    @GetMapping("/setup")
    public SetupDto setup(
            @RequestParam @NotNull Long accountId,
            @RequestParam(required = false) @Min(1) Integer days,
            @RequestParam(required = false) @Min(1) Integer notificationsLimit,
            @RequestParam(required = false) @Min(1) Integer topCategories
    ) {
        return service.get(accountId, days, notificationsLimit, topCategories);
    }
}
