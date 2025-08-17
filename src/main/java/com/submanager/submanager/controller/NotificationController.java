package com.submanager.submanager.controller;

import com.submanager.submanager.dto.record.NotificationDto;
import com.submanager.submanager.model.enums.NotificationStatus;
import com.submanager.submanager.service.NotificationService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }

    // Listar notificaciones por cuenta (status opcional: PENDING | READ)
    @GetMapping
    public List<NotificationDto> list(
            @RequestParam @NotNull Long accountId,
            @RequestParam(required = false) NotificationStatus status
    ) {
        return service.list(accountId, status);
    }

    // Marcar como leída
    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long id) {
        service.markAsRead(id);
    }
}
