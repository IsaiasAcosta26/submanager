package com.submanager.submanager.dto.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagDto(
        Long id,
        @NotBlank @Size(min = 2, max = 80) String name,
        @Size(max = 255) String description
) {}
