package com.library.domain.user;

import com.library.domain.user.dto.UserResponse;
import org.mapstruct.Mapper;

/**
 * Entity to DTO mapping. Component model is set globally to Spring in the compiler config.
 */
@Mapper
public interface UserMapper {

    UserResponse toResponse(User user);
}
