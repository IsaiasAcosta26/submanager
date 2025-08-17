package com.submanager.submanager.jobs;

import com.submanager.submanager.dto.record.SubscriptionDto;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.repository.AccountRepository;
import com.submanager.submanager.service.NotificationService;
import com.submanager.submanager.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class RenewalReminderJob {

    private static final Logger log = LoggerFactory.getLogger(RenewalReminderJob.class);

    private final AccountRepository accountRepo;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    @Value("${subsmart.reminders.daysAhead:7}")
    private int daysAhead;

    public RenewalReminderJob(AccountRepository accountRepo,
                              SubscriptionService subscriptionService,
                              NotificationService notificationService) {
        this.accountRepo = accountRepo;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
    }

    // Corre según cron (propiedad configurable). Por defecto: 9:00am todos los días.
    @Scheduled(cron = "${subsmart.reminders.cron:0 0 9 * * *}")
    public void runDaily() {
        runInternal("cron");
    }

    // Método reutilizable (también expuesto por un endpoint manual de prueba)
    public void runInternal(String trigger) {
        var fmt = DateTimeFormatter.ISO_DATE;
        var accounts = accountRepo.findAll();
        int created = 0;
        for (var acc : accounts) {
            var upcoming = subscriptionService.upcomingRenewals(acc.getId(), daysAhead, SubscriptionStatus.ACTIVE);
            for (SubscriptionDto s : upcoming) {
                if (s.nextRenewalDate() == null) continue;
                var title = "Renovación próxima: " + s.name();
                var message = "Tu plan " + (s.plan() != null ? s.plan() : "") +
                        " de " + s.provider() + " renueva el " + s.nextRenewalDate().format(fmt) +
                        ". Precio ~" + s.price() + " " + s.currency() + ".";
                var dto = notificationService.createRenewalReminder(
                        s.accountId(), s.id(), title, message, s.nextRenewalDate()
                );
                if (dto != null) created++;
            }
        }
        log.info("[RenewalReminderJob] trigger={}, accounts={}, reminders_created={}", trigger, accounts.size(), created);
    }
}
