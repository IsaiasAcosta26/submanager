package com.submanager.submanager.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter @Setter
public class Account extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 80)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 120)
    private String email;
}
