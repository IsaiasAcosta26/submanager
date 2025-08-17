package com.submanager.submanager.repository;

import com.submanager.submanager.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByNameIgnoreCase(String name);
}
