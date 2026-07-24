package com.library.domain.publisher;

import com.library.domain.publisher.dto.PublisherRequest;
import com.library.domain.publisher.dto.PublisherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PublisherMapper {

    PublisherResponse toResponse(Publisher entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PublisherRequest request, @MappingTarget Publisher entity);
}
