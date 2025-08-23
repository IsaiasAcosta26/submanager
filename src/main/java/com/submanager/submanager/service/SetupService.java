package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.DashboardDto;
import com.submanager.submanager.dto.record.SetupDto;
import com.submanager.submanager.model.entity.Account;
import com.submanager.submanager.model.entity.Category;
import com.submanager.submanager.model.entity.Tag;
import com.submanager.submanager.repository.AccountRepository;
import com.submanager.submanager.repository.CategoryRepository;
import com.submanager.submanager.repository.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SetupService {

    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;
    private final DashboardService dashboardService;

    @Value("${subsmart.reminders.daysAhead:7}")
    private int remindersDaysAhead;

    @Value("${subsmart.savings.highCost:15}")
    private java.math.BigDecimal savingsHighCost;

    @Value("${subsmart.savings.inactiveDays:60}")
    private int savingsInactiveDays;

    @Value("${dashboard.upcomingDays:7}")
    private int dashboardUpcomingDays;

    @Value("${dashboard.notificationsLimit:5}")
    private int dashboardNotificationsLimit;

    @Value("${dashboard.topCategories:5}")
    private int dashboardTopCategories;

    public SetupService(AccountRepository accountRepo,
                        CategoryRepository categoryRepo,
                        TagRepository tagRepo,
                        DashboardService dashboardService) {
        this.accountRepo = accountRepo;
        this.categoryRepo = categoryRepo;
        this.tagRepo = tagRepo;
        this.dashboardService = dashboardService;
    }

    public SetupDto get(Long accountId,
                        Integer days, Integer notificationsLimit, Integer topCategories) {

        Account acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("account no encontrado"));

        // Ajustes efectivos (param > property por si el móvil quiere customizar)
        int effDays = (days == null || days < 1) ? dashboardUpcomingDays : days;
        int effNotif = (notificationsLimit == null || notificationsLimit < 1) ? dashboardNotificationsLimit : notificationsLimit;
        int effTop = (topCategories == null || topCategories < 1) ? dashboardTopCategories : topCategories;

        // Dashboard snapshot reutilizando tu servicio existente
        DashboardDto dashboard = dashboardService.get(accountId, effDays, effNotif, effTop);

        // Catálogos (ordenados por nombre asc)
        List<SetupDto.CategoryItem> categories = categoryRepo.findAll().stream()
                .sorted(Comparator.comparing(c -> nz(((Category) c).getName()).toLowerCase()))
                .map(c -> new SetupDto.CategoryItem(
                        c.getId(), c.getName(), c.getColor(), c.getDescription()
                )).toList();

        List<SetupDto.TagItem> tags = tagRepo.findAll().stream()
                .sorted(Comparator.comparing(t -> nz(((Tag) t).getName()).toLowerCase()))
                .map(t -> new SetupDto.TagItem(
                        t.getId(), t.getName(), t.getDescription()
                )).toList();

        var settings = new SetupDto.Settings(
                remindersDaysAhead,
                savingsHighCost,
                savingsInactiveDays,
                effDays,
                effNotif,
                effTop
        );

        var account = new SetupDto.AccountInfo(acc.getId(), acc.getName(), acc.getEmail());

        return new SetupDto(account, settings, categories, tags, dashboard);
    }

    private String nz(String s) { return s == null ? "" : s; }
}
