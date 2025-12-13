package com.dmcdoc.usermanagement.core.mapper;

import com.dmcdoc.sharedcommon.dto.UserDto;
import com.dmcdoc.sharedcommon.dto.UserResponse;
import com.dmcdoc.usermanagement.core.model.User;

import java.util.stream.Collectors;

public class UserMapper {
    public static UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getCreatedAt(),
                !u.isEnabled(),
                u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()));
    }

    public static UserResponse toResponse(User u) {
        if (u == null)
            return null;
        return UserResponse.builder()
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .build();
    }
}
