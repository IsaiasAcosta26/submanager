package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.ImportRowError;
import com.submanager.submanager.dto.record.ImportSubscriptionsResultDto;
import com.submanager.submanager.dto.record.SubscriptionDto;
import com.submanager.submanager.model.entity.Category;
import com.submanager.submanager.model.entity.Tag;
import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import com.submanager.submanager.repository.AccountRepository;
import com.submanager.submanager.repository.CategoryRepository;
import com.submanager.submanager.repository.TagRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImportService {

    private final SubscriptionService subscriptionService;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;

    public ImportService(SubscriptionService subscriptionService,
                         AccountRepository accountRepo,
                         CategoryRepository categoryRepo,
                         TagRepository tagRepo) {
        this.subscriptionService = subscriptionService;
        this.accountRepo = accountRepo;
        this.categoryRepo = categoryRepo;
        this.tagRepo = tagRepo;
    }

    /**
     * Cabeceras esperadas (insensibles a may/minus y con trim).
     * Puedes agregar columnas extra; solo leemos estas.
     */
    private static final List<String> EXPECTED = List.of(
            "accountId","name","provider","plan","price","currency","billingCycle",
            "nextRenewalDate","status","lastActivityDate","notes","categoryName","tags"
    );

    @Transactional
    public ImportSubscriptionsResultDto importSubscriptions(MultipartFile file, boolean dryRun) throws Exception {
        if (file == null || file.isEmpty()) {
            return new ImportSubscriptionsResultDto(0, 0, 0, List.of(new ImportRowError(0, "archivo vacío")));
        }

        int total = 0, ok = 0, fail = 0;
        List<ImportRowError> errors = new ArrayList<>();

        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             var parser = CSVParser.parse(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreEmptyLines()
                     .withTrim())) {

            // Validar cabeceras mínimas
            var headerLower = parser.getHeaderMap().keySet().stream()
                    .map(h -> h.trim().toLowerCase()).collect(Collectors.toSet());
            for (String need : EXPECTED) {
                if (!headerLower.contains(need.toLowerCase())) {
                    return new ImportSubscriptionsResultDto(0, 0, 0,
                            List.of(new ImportRowError(0, "falta columna requerida: " + need)));
                }
            }

            int rowNum = 1; // header = 1
            for (CSVRecord r : parser) {
                rowNum++;
                total++;

                try {
                    // 1) Lectura segura
                    Long accountId = parseLong(req(r, "accountId"));
                    String name = req(r, "name");
                    String provider = req(r, "provider");
                    String plan = opt(r, "plan");
                    BigDecimal price = new BigDecimal(req(r, "price"));
                    String currency = req(r, "currency");
                    BillingCycle billing = BillingCycle.valueOf(req(r, "billingCycle").toUpperCase());
                    LocalDate nextRenewal = parseDate(opt(r, "nextRenewalDate"));
                    SubscriptionStatus status = parseStatus(opt(r, "status"));
                    LocalDate lastActivity = parseDate(opt(r, "lastActivityDate"));
                    String notes = opt(r, "notes");
                    String categoryName = opt(r, "categoryName");
                    String tagsStr = opt(r, "tags"); // "Trabajo;Familiar" o "Trabajo,Familiar"

                    // 2) Validaciones mínimas
                    if (accountId == null || !accountRepo.existsById(accountId)) {
                        throw new IllegalArgumentException("accountId inválido o inexistente");
                    }
                    if (price.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("price no puede ser negativo");
                    }

                    // 3) Resolver asociaciones (crear si no existen)
                    Long categoryId = null;
                    if (categoryName != null && !categoryName.isBlank()) {
                        Category c = categoryRepo.findByNameIgnoreCase(categoryName.trim())
                                .orElseGet(() -> {
                                    Category nc = new Category();
                                    nc.setName(categoryName.trim());
                                    return categoryRepo.save(nc);
                                });
                        categoryId = c.getId();
                    }

                    List<Long> tagIds = new ArrayList<>();
                    if (tagsStr != null && !tagsStr.isBlank()) {
                        String[] parts = tagsStr.split("[;,]");
                        for (String p : parts) {
                            String tname = p.trim();
                            if (tname.isEmpty()) continue;
                            Tag t = tagRepo.findByNameIgnoreCase(tname)
                                    .orElseGet(() -> {
                                        Tag nt = new Tag();
                                        nt.setName(tname);
                                        return tagRepo.save(nt);
                                    });
                            tagIds.add(t.getId());
                        }
                    }

                    // 4) Armar DTO y persistir (o dry-run)
                    var dto = new SubscriptionDto(
                            null,                // id
                            accountId,
                            name,
                            provider,
                            plan,
                            price,
                            currency,
                            billing,
                            nextRenewal,
                            status != null ? status : SubscriptionStatus.ACTIVE,
                            lastActivity,
                            notes,
                            categoryId,
                            tagIds
                    );

                    if (!dryRun) {
                        subscriptionService.create(dto);
                    }

                    ok++;
                } catch (Exception e) {
                    fail++;
                    errors.add(new ImportRowError(rowNum, e.getMessage()));
                }
            }
        }

        return new ImportSubscriptionsResultDto(total, ok, fail, errors);
    }

    // -------- helpers --------
    private String req(CSVRecord r, String col) {
        String v = r.get(col);
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException("columna requerida vacía: " + col);
        return v.trim();
    }
    private String opt(CSVRecord r, String col) {
        String v = r.get(col);
        return v == null ? null : v.trim();
    }
    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        return Long.valueOf(s);
    }
    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s); // ISO-8601 yyyy-MM-dd
    }
    private SubscriptionStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        return SubscriptionStatus.valueOf(s.toUpperCase());
    }
}
