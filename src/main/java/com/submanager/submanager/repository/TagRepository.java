package com.submanager.submanager.repository;

import com.submanager.submanager.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Tag> findByNameIgnoreCase(String name);
}
