package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Role;

public interface RoleService {

    Role getOrCreate(String roleName);

}
