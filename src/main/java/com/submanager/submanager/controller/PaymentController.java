package com.submanager.submanager.controller;

import com.submanager.submanager.dto.record.PaymentDto;
import com.submanager.submanager.dto.record.PaymentMockRequestDTO;
import com.submanager.submanager.dto.record.PaymentResponseDTO;
import com.submanager.submanager.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    // Simula un cobro (como si fuera el webhook del proveedor)
    @PostMapping("/mock/charge")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDTO mockCharge(@RequestBody PaymentMockRequestDTO request) {
        return service.mockCharge(request);
    }

    // Lista los pagos de una suscripción (útil para depurar en móvil)
    @GetMapping
    public List<PaymentDto> list(@RequestParam Long subscriptionId) {
        return service.listBySubscription(subscriptionId);
    }
}
