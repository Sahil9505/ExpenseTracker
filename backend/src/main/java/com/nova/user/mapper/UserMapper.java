package com.nova.user.mapper;

import com.nova.user.User;
import com.nova.user.web.dto.UserResponse;
import org.mapstruct.Mapper;

/**
 * Maps the {@link User} entity to the API-safe {@link UserResponse}. The password
 * hash is never part of the target projection.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
