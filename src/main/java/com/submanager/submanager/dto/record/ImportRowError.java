package com.submanager.submanager.dto.record;

public record ImportRowError(
        int row,
        String error
) {}
