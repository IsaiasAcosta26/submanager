package com.submanager.submanager.controller;

import com.submanager.submanager.dto.record.ImportSubscriptionsResultDto;
import com.submanager.submanager.service.ImportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/import")
public class ImportController {

    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    // POST /api/v1/import/subscriptions?dryRun=true|false
    @PostMapping(value = "/subscriptions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportSubscriptionsResultDto importSubscriptions(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun
    ) throws Exception {
        return service.importSubscriptions(file, dryRun);
    }

    // GET /api/v1/import/subscriptions/sample  -> CSV de ejemplo
    @GetMapping(value = "/subscriptions/sample", produces = "text/csv")
    public ResponseEntity<ByteArrayResource> sample() {
        String csv =
                "accountId,name,provider,plan,price,currency,billingCycle,nextRenewalDate,status,lastActivityDate,notes,categoryName,tags\n" +
                        "1,Netflix,Netflix,Premium,15.99,USD,MONTHLY,2025-09-01,ACTIVE,2025-07-15,Uso familiar,Streaming,\"Familiar;Entretenimiento\"\n" +
                        "1,Spotify,Spotify,Individual,9.99,USD,MONTHLY,2025-08-28,ACTIVE,2025-08-01,Musica diaria,Musica,\"Trabajo;Audio\"\n" +
                        "1,JetBrains,JetBrains,All Products,199.00,USD,YEARLY,2026-02-01,ACTIVE,2025-07-30,IDE anual,Software,Trabajo\n";

        var bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var res = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"subscriptions_sample.csv\"")
                .contentLength(bytes.length)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(res);
    }
}
