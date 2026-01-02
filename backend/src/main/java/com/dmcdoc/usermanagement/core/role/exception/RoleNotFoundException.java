package com.dmcdoc.usermanagement.core.role.exception;

import java.util.UUID;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(UUID roleId) {
        super("Role not found with id " + roleId);
    }
}