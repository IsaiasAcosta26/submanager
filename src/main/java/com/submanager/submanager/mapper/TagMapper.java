package com.submanager.submanager.mapper;

import com.submanager.submanager.dto.record.TagDto;
import com.submanager.submanager.model.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    TagDto toDto(Tag entity);
    Tag toEntity(TagDto dto);
}
