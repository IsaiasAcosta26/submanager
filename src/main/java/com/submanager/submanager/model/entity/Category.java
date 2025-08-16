package com.submanager.submanager.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter @Setter
public class Category extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String name;        // p.ej. "Streaming", "Música", "Software"

    @Column(length = 16)
    private String color;       // p.ej. "#FF4081" (opcional)

    @Column(length = 255)
    private String description; // opcional
}
