package com.submanager.submanager.controller;

import com.submanager.submanager.dto.AccountDto;
import com.submanager.submanager.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService service;
    public AccountController(AccountService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto create(@RequestBody @Valid AccountDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public AccountDto get(@PathVariable Long id) {
        return service.get(id);
    }
}
