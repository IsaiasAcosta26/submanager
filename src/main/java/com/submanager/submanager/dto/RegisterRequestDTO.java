package com.submanager.submanager.dto;

import com.submanager.submanager.model.enums.Role;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private Role role;
}