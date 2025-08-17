package com.submanager.submanager.mapper;

import com.submanager.submanager.dto.record.SubscriptionDto;
import com.submanager.submanager.model.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    // Entity -> DTO (mapeamos asociaciones a IDs)
    @Mappings({
            @Mapping(target = "accountId", source = "account.id"),
            @Mapping(target = "categoryId", source = "category.id"),
            @Mapping(
                    target = "tagIds",
                    expression =
                            // usamos FQCN para Tag para evitar problemas de imports
                            "java( s.getTags()==null ? java.util.List.of() : " +
                                    "s.getTags().stream().map(com.submanager.submanager.model.entity.Tag::getId).toList() )"
            )
    })
    SubscriptionDto toDto(Subscription s);

    // DTO -> Entity (solo escalares; associations las setea el Service)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "category", ignore = true),
            @Mapping(target = "tags", ignore = true)
    })
    Subscription toEntity(SubscriptionDto dto);

    // Merge para update (ignora associations e id)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "category", ignore = true),
            @Mapping(target = "tags", ignore = true)
    })
    void updateEntityFromDto(SubscriptionDto dto, @MappingTarget Subscription entity);
}
