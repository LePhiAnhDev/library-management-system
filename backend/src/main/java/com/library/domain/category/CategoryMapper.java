package com.library.domain.category;

import com.library.domain.category.dto.CategoryRequest;
import com.library.domain.category.dto.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Category entity to/from DTO. Scalar fields are copied by MapStruct; the parent relationship,
 * status and audit fields are managed by the service.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    CategoryResponse toResponse(Category entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CategoryRequest request, @MappingTarget Category entity);
}
