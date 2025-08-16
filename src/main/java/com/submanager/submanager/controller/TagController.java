package com.submanager.submanager.controller;

import com.submanager.submanager.dto.record.TagDto;
import com.submanager.submanager.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService service;
    public TagController(TagService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagDto create(@RequestBody @Valid TagDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public TagDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<TagDto> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public TagDto update(@PathVariable Long id, @RequestBody @Valid TagDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
