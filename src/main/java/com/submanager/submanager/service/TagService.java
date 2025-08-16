package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.TagDto;
import com.submanager.submanager.mapper.TagMapper;
import com.submanager.submanager.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TagService {

    private final TagRepository repo;
    private final TagMapper mapper;

    public TagService(TagRepository repo, TagMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public TagDto create(TagDto dto) {
        if (repo.existsByNameIgnoreCase(dto.name())) throw new IllegalArgumentException("tag ya existe");
        var t = mapper.toEntity(dto);
        repo.save(t);
        return mapper.toDto(t);
    }

    @Transactional(readOnly = true)
    public TagDto get(Long id) {
        var t = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("tag no encontrado"));
        return mapper.toDto(t);
    }

    @Transactional(readOnly = true)
    public List<TagDto> list() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    public TagDto update(Long id, TagDto dto) {
        var t = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("tag no encontrado"));
        if (!t.getName().equalsIgnoreCase(dto.name()) && repo.existsByNameIgnoreCase(dto.name()))
            throw new IllegalArgumentException("tag ya existe");
        t.setName(dto.name());
        t.setDescription(dto.description());
        return mapper.toDto(t);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("tag no encontrado");
        repo.deleteById(id);
    }
}