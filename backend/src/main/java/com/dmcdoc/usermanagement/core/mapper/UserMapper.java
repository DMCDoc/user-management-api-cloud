package com.dmcdoc.usermanagement.core.mapper;

import com.dmcdoc.sharedcommon.dto.UserResponse;
import com.dmcdoc.usermanagement.core.model.User;

public final class UserMapper {

    private UserMapper() {
        // utility class
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .active(user.isActive())
                .build();
    }
}
