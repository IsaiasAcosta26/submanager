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

    // Listar (status opcional) + límite opcional
    @GetMapping
    public List<NotificationDto> list(
            @RequestParam @NotNull Long accountId,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) Integer limit
    ) {
        return service.list(accountId, status, limit);
    }

    // Contador de no leídas
    @GetMapping("/count")
    public long unreadCount(@RequestParam @NotNull Long accountId) {
        return service.unreadCount(accountId);
    }

    // Marcar una como leída (ya lo tenías)
    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long id) {
        service.markAsRead(id);
    }

    // NUEVO: Marcar todas como leídas
    @PatchMapping("/read-all")
    public int markAllRead(@RequestParam @NotNull Long accountId) {
        return service.markAllAsRead(accountId);
    }

    // NUEVO: Marcar varias por IDs
    @PatchMapping("/read")
    public int markManyRead(@RequestBody List<Long> ids) {
        return service.markManyAsRead(ids);
    }

    // NUEVO: Borrar todas las leídas
    @DeleteMapping("/read")
    public int deleteRead(@RequestParam @NotNull Long accountId) {
        return service.deleteAllRead(accountId);
    }
}
