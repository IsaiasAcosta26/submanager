package com.submanager.submanager.dto;

import com.submanager.submanager.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequestDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    private Role role;
}