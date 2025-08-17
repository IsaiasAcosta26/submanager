//package com.submanager.submanager.controller;
//
//import com.submanager.submanager.dto.MonthlySummaryResponseDTO;
//import com.submanager.submanager.dto.SubscriptionRequestDTO;
//import com.submanager.submanager.dto.SubscriptionResponseDTO;
//import com.submanager.submanager.service.SubscriptionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/subscriptions")
//@RequiredArgsConstructor
//public class SubscriptionController {
//
//    private final SubscriptionService service;
//
//    @PostMapping
//    public ResponseEntity<SubscriptionResponseDTO> create(@RequestBody SubscriptionRequestDTO request) {
//        return ResponseEntity.ok(service.create(request));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<SubscriptionResponseDTO>> getAll() {
//        return ResponseEntity.ok(service.getAllByCurrentUser());
//    }
//
//    @PutMapping("/{id}/pay")
//    public ResponseEntity<String> markAsPaid(@PathVariable Long id) {
//        service.markAsPaid(id);
//        return ResponseEntity.ok("Suscripción marcada como pagada.");
//    }
//
//    @GetMapping("/summary")
//    public ResponseEntity<MonthlySummaryResponseDTO> getSummary() {
//        return ResponseEntity.ok(service.getMonthlySummary());
//    }
//
//
//}
