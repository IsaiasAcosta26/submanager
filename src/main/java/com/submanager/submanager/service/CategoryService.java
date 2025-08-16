package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.CategoryDto;
import com.submanager.submanager.model.entity.Category;
import com.submanager.submanager.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) {
        this.repo = repo;
    }

    public CategoryDto create(CategoryDto dto) {
        if (repo.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("category ya existe");
        }
        var c = new Category();
        c.setName(dto.name());
        c.setColor(dto.color());
        c.setDescription(dto.description());
        repo.save(c);
        return toDto(c);
    }

    @Transactional(readOnly = true)
    public CategoryDto get(Long id) {
        var c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("category no encontrada"));
        return toDto(c);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public CategoryDto update(Long id, CategoryDto dto) {
        var c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("category no encontrada"));
        if (!c.getName().equalsIgnoreCase(dto.name()) && repo.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("category ya existe");
        }
        c.setName(dto.name());
        c.setColor(dto.color());
        c.setDescription(dto.description());
        return toDto(c);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("category no encontrada");
        repo.deleteById(id);
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getColor(), c.getDescription());
    }
}
