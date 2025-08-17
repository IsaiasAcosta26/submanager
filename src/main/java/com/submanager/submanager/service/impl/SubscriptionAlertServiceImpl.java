//package com.submanager.submanager.service.impl;
//
//import com.submanager.submanager.model.entity.Subscription;
//import com.submanager.submanager.repository.SubscriptionRepository;
//import com.submanager.submanager.service.SubscriptionAlertService;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SubscriptionAlertServiceImpl implements SubscriptionAlertService {
//
//    private final SubscriptionRepository subscriptionRepository;
//
//    @Override
//    @Transactional
//    @Scheduled(cron = "0 0 8 * * *") // Ejecuta todos los días a las 8:00 a.m.
//    public void checkUpcomingPayments() {
//        LocalDate now = LocalDate.now();
//        LocalDate in3Days = now.plusDays(3);
//
//        List<Subscription> upcoming = subscriptionRepository.findAll().stream()
//                .filter(s -> !s.isPaid() && s.getPaymentDate().isAfter(now.minusDays(1))
//                        && s.getPaymentDate().isBefore(in3Days.plusDays(1)))
//                .toList();
//
//        upcoming.forEach(s -> log.info("ALERTA: La suscripción '{}' vence el {}", s.getServiceName(), s.getPaymentDate()));
//        // Más adelante podemos enviar correo o WhatsApp
//    }
//}
