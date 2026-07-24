package com.library.domain.member;

import com.library.domain.member.dto.MemberRequest;
import com.library.domain.member.dto.MemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    MemberResponse toResponse(Member entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "memberCode", ignore = true)
    @Mapping(target = "joinDate", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(MemberRequest request, @MappingTarget Member entity);
}
