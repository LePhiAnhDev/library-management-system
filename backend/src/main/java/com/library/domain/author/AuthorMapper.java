package com.library.domain.author;

import com.library.domain.author.dto.AuthorRequest;
import com.library.domain.author.dto.AuthorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorResponse toResponse(Author entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(AuthorRequest request, @MappingTarget Author entity);
}
