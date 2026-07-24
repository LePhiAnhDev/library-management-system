package com.library.domain.user;

import com.library.domain.user.dto.UserResponse;
import org.mapstruct.Mapper;

/**
 * Entity to DTO mapping, generated as a Spring bean.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
