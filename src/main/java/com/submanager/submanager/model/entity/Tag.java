package com.submanager.submanager.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tags")
@Getter @Setter
public class Tag extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String name;        // p.ej. "Familiar", "Trabajo", "Estudio"

    @Column(length = 255)
    private String description; // opcional
}
