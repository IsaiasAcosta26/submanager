package com.submanager.submanager.mapper;

import com.submanager.submanager.dto.record.AccountDto;
import com.submanager.submanager.model.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)public interface AccountMapper {
    AccountDto toDto(Account entity);
    Account toEntity(AccountDto dto);
}
