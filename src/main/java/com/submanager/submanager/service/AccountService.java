package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.AccountDto;
import com.submanager.submanager.model.entity.Account;
import com.submanager.submanager.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {

    private final AccountRepository repo;

    public AccountService(AccountRepository repo) {
        this.repo = repo;
    }

    public AccountDto create(AccountDto dto) {
        if (repo.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("email ya existe");
        }
        var acc = new Account();
        acc.setName(dto.name());
        acc.setEmail(dto.email());
        repo.save(acc);
        return new AccountDto(acc.getId(), acc.getName(), acc.getEmail());
    }

    @Transactional(readOnly = true)
    public AccountDto get(Long id) {
        var a = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
        return new AccountDto(a.getId(), a.getName(), a.getEmail());
    }
}
