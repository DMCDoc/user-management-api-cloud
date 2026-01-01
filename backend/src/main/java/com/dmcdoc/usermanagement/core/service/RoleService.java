package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Role;
import java.util.UUID;



public interface RoleService {

    Role getOrCreate(String roleName);

    Role create(Role role);

    void delete(UUID roleId);
}
