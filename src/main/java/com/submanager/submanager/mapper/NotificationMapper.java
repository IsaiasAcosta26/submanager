package com.submanager.submanager.mapper;

import com.submanager.submanager.dto.record.NotificationDto;
import com.submanager.submanager.model.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "subscriptionId", source = "subscription.id")
    NotificationDto toDto(Notification n);
}
