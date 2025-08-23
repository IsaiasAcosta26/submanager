package com.submanager.submanager.controller;

import com.submanager.submanager.model.entity.Account;
import com.submanager.submanager.model.entity.Category;
import com.submanager.submanager.model.entity.Subscription;
import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.repository.AccountRepository;
import com.submanager.submanager.repository.CategoryRepository;
import com.submanager.submanager.repository.SubscriptionRepository;
import com.submanager.submanager.repository.TagRepository;
import com.submanager.submanager.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ops")
public class OpsController {

    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final NotificationService notificationService;

    @Value("${subsmart.reminders.daysAhead:7}")
    private int remindersDaysAhead;

    @Value("${subsmart.savings.highCost:15}")
    private java.math.BigDecimal savingsHighCost;

    public OpsController(AccountRepository accountRepo,
                         CategoryRepository categoryRepo,
                         TagRepository tagRepo,
                         SubscriptionRepository subscriptionRepo,
                         NotificationService notificationService) {
        this.accountRepo = accountRepo;
        this.categoryRepo = categoryRepo;
        this.tagRepo = tagRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.notificationService = notificationService;
    }

    /** Seed mínimo para DEV: crea cuenta demo + categorías + 2 suscripciones. */
    @PostMapping("/dev/seed")
    @Transactional
    public Map<String, Object> seed() {
        Account acc = accountRepo.findAll().stream().findFirst().orElse(null);
        if (acc == null) {
            acc = new Account();
            acc.setName("Demo User");
            acc.setEmail("demo@submanager.local");
            accountRepo.save(acc);

            Category streaming = new Category();
            streaming.setName("Streaming");
            streaming.setColor("#FF5A5F");
            streaming.setDescription("Video / Series");
            categoryRepo.save(streaming);

            Category music = new Category();
            music.setName("Música");
            music.setColor("#3B82F6");
            music.setDescription("Audio");
            categoryRepo.save(music);

            Subscription netflix = new Subscription();
            netflix.setAccount(acc);
            netflix.setCategory(streaming);
            netflix.setProvider("Netflix");
            netflix.setName("Netflix");
            netflix.setCurrency("USD");
            netflix.setPrice(new java.math.BigDecimal("15.99"));
            netflix.setBillingCycle(BillingCycle.MONTHLY);
            netflix.setStatus(SubscriptionStatus.ACTIVE);
            netflix.setNextRenewalDate(LocalDate.now().plusDays(3));
            subscriptionRepo.save(netflix);

            Subscription spotify = new Subscription();
            spotify.setAccount(acc);
            spotify.setCategory(music);
            spotify.setProvider("Spotify");
            spotify.setName("Spotify");
            spotify.setCurrency("USD");
            spotify.setPrice(new java.math.BigDecimal("9.99"));
            spotify.setBillingCycle(BillingCycle.MONTHLY);
            spotify.setStatus(SubscriptionStatus.ACTIVE);
            spotify.setNextRenewalDate(LocalDate.now().plusDays(10));
            subscriptionRepo.save(spotify);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("accountId", acc.getId());
        return res;
    }

    /** Ayuda: devuelve el primer accountId si ya existe alguno. */
    @GetMapping("/dev/first-account")
    @Transactional(readOnly = true)
    public Map<String, Object> firstAccount() {
        Long id = accountRepo.findAll().stream().map(Account::getId).sorted().findFirst().orElse(null);
        Map<String, Object> res = new HashMap<>();
        res.put("accountId", id);
        return res;
    }

    /** Ejecuta recordatorios de renovación “ahora”. */
    @PostMapping("/run-reminders")
    @Transactional
    public Map<String, Object> runReminders() {
        LocalDate until = LocalDate.now().plusDays(remindersDaysAhead);
        int created = 0;

        for (Subscription s : subscriptionRepo.findAll()) {
            if (s.getStatus() == SubscriptionStatus.ACTIVE
                    && s.getNextRenewalDate() != null
                    && !s.getNextRenewalDate().isAfter(until)) {
                String title = "Próxima renovación: " + (s.getName() != null ? s.getName() : s.getProvider());
                String msg = "Renueva el " + s.getNextRenewalDate();
                var dto = notificationService.createRenewalReminder(
                        s.getAccount().getId(), s.getId(), title, msg, s.getNextRenewalDate());
                if (dto != null) created++;
            }
        }
        return Map.of("created", created, "daysAhead", remindersDaysAhead);
    }

    /** Ejecuta sugerencias de ahorro simples. */
    @PostMapping("/run-savings")
    @Transactional
    public Map<String, Object> runSavings() {
        int created = 0;

        for (Subscription s : subscriptionRepo.findAll()) {
            if (s.getStatus() == SubscriptionStatus.ACTIVE && s.getPrice() != null) {
                var title = "Posible ahorro: " + (s.getName() != null ? s.getName() : s.getProvider());
                var msg = "Costo mensual " + s.getPrice() + " " + (s.getCurrency() != null ? s.getCurrency() : "")
                        + ". Revisa plan o baja.";
                if (s.getPrice().compareTo(savingsHighCost) >= 0) {
                    var dto = notificationService.createSavingSuggestion(
                            s.getAccount().getId(), s.getId(), title, msg);
                    if (dto != null) created++;
                }
            }
        }
        return Map.of("created", created, "highCost", savingsHighCost);
    }
}
