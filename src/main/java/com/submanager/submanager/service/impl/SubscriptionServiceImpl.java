//package com.submanager.submanager.service.impl;
//
//import com.submanager.submanager.dto.MonthlySummaryResponseDTO;
//import com.submanager.submanager.dto.SubscriptionRequestDTO;
//import com.submanager.submanager.dto.SubscriptionResponseDTO;
//import com.submanager.submanager.model.entity.Subscription;
//import com.submanager.submanager.model.entity.User;
//import com.submanager.submanager.repository.SubscriptionRepository;
//import com.submanager.submanager.repository.UserRepository;
//import com.submanager.submanager.service.SubscriptionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static java.util.stream.Collectors.toList;
//
//@Service
//@RequiredArgsConstructor
//public class SubscriptionServiceImpl implements SubscriptionService {
//
//    private final SubscriptionRepository subscriptionRepo;
//    private final UserRepository userRepo;
//
//    @Override
//    public SubscriptionResponseDTO create(SubscriptionRequestDTO request) {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User user = userRepo.findByUsername(username).orElseThrow();
//
//        Subscription sub = Subscription.builder()
//                .serviceName(request.getServiceName())
//                .amount(request.getAmount())
//                .paymentDate(request.getPaymentDate())
//                .isPaid(false)
//                .user(user)
//                .build();
//
//        Subscription saved = subscriptionRepo.save(sub);
//        return mapToResponse(saved);
//    }
//
//    @Override
//    public List<SubscriptionResponseDTO> getAllByCurrentUser() {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User user = userRepo.findByUsername(username).orElseThrow();
//        return subscriptionRepo.findByUser(user)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(toList());
//    }
//
//    private SubscriptionResponseDTO mapToResponse(Subscription s) {
//        return new SubscriptionResponseDTO(
//                s.getId(), s.getServiceName(), s.getAmount(), s.getPaymentDate(), s.isPaid()
//        );
//    }
//
//    @Override
//    public void markAsPaid(Long id) {
//        Subscription subscription = subscriptionRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("No se encontró la suscripción"));
//
//        subscription.setPaid(true);
//        subscriptionRepo.save(subscription);
//    }
//
//    @Override
//    public MonthlySummaryResponseDTO getMonthlySummary() {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User user = userRepo.findByUsername(username).orElseThrow();
//
//        List<Subscription> subscriptions = subscriptionRepo.findByUser(user);
//
//        BigDecimal total = subscriptions.stream()
//                .map(Subscription::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal paid = subscriptions.stream()
//                .filter(Subscription::isPaid)
//                .map(Subscription::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal pending = total.subtract(paid);
//
//        return new MonthlySummaryResponseDTO(total, paid, pending);
//    }
//
//}
