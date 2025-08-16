package com.submanager.submanager.service;

import com.submanager.submanager.dto.record.AccountDto;
import com.submanager.submanager.mapper.AccountMapper;
import com.submanager.submanager.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {

    private final AccountRepository repo;
    private final AccountMapper mapper;

    public AccountService(AccountRepository repo, AccountMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public AccountDto create(AccountDto dto) {
        if (repo.existsByEmail(dto.email())) throw new IllegalArgumentException("email ya existe");
        var entity = mapper.toEntity(dto);
        repo.save(entity);
        return mapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public AccountDto get(Long id) {
        var a = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("account no encontrado"));
        return mapper.toDto(a);
    }
}
