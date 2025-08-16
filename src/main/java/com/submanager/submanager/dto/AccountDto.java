package com.submanager.submanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountDto(
        Long id,
        @NotBlank @Size(min = 2, max = 80) String name,
        @NotBlank @Email String email
) {}
