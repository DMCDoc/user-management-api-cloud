package com.dmcdoc.usermanagement.core.mapper;

import com.dmcdoc.sharedcommon.dto.AdminUserResponse;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;

import java.util.Set;
import java.util.stream.Collectors;

public final class AdminUserMapper {

    private AdminUserMapper() {
    }

    public static AdminUserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        AdminUserResponse dto = new AdminUserResponse();
        dto.setId(user.getId());
        dto.setTenantId(user.getTenantId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setActive(user.isActive());
        dto.setLocked(user.isLocked());
        dto.setRoles(mapRoles(user.getRoles()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }

    private static Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
