package com.submanager.submanager.controller;

import com.submanager.submanager.dto.record.CategoryDto;
import com.submanager.submanager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid CategoryDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public CategoryDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<CategoryDto> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public CategoryDto update(@PathVariable Long id, @RequestBody @Valid CategoryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
