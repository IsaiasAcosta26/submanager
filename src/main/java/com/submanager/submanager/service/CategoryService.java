package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.CategoryDto;
import com.submanager.submanager.mapper.CategoryMapper;
import com.submanager.submanager.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository repo;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository repo, CategoryMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public CategoryDto create(CategoryDto dto) {
        if (repo.existsByNameIgnoreCase(dto.name())) throw new IllegalArgumentException("category ya existe");
        var c = mapper.toEntity(dto);
        repo.save(c);
        return mapper.toDto(c);
    }

    @Transactional(readOnly = true)
    public CategoryDto get(Long id) {
        var c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("category no encontrada"));
        return mapper.toDto(c);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> list() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    public CategoryDto update(Long id, CategoryDto dto) {
        var c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("category no encontrada"));
        if (!c.getName().equalsIgnoreCase(dto.name()) && repo.existsByNameIgnoreCase(dto.name()))
            throw new IllegalArgumentException("category ya existe");
        // merge manual simple (pocas props)
        c.setName(dto.name());
        c.setColor(dto.color());
        c.setDescription(dto.description());
        return mapper.toDto(c);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("category no encontrada");
        repo.deleteById(id);
    }
}
