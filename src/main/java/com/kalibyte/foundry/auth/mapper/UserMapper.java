package com.kalibyte.foundry.auth.mapper;

import com.kalibyte.foundry.auth.dto.UserResponse;
import com.kalibyte.foundry.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(role -> role.getName().name()).toList())")
    UserResponse toResponse(User user);
}
