package com.submanager.submanager.dto.record;

import java.util.List;

public record ImportSubscriptionsResultDto(
        int totalRows,
        int imported,
        int failed,
        List<ImportRowError> errors
) {}
