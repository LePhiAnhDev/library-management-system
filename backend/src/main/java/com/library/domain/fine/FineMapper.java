package com.library.domain.fine;

import com.library.domain.fine.dto.FineResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FineMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberCode", source = "member.memberCode")
    @Mapping(target = "memberName", source = "member.fullName")
    @Mapping(target = "loanId", source = "loan.id")
    @Mapping(target = "loanCode", source = "loan.code")
    @Mapping(target = "settledById", source = "settledBy.id")
    @Mapping(target = "settledByName", source = "settledBy.fullName")
    FineResponse toResponse(Fine fine);
}
