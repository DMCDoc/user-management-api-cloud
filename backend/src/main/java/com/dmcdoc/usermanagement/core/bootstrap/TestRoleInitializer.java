package com.dmcdoc.usermanagement.core.bootstrap;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;

import jakarta.annotation.PostConstruct;

@Component
@Profile("test")
public class TestRoleInitializer {

    public static final UUID SYSTEM_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final RoleRepository roleRepository;

    public TestRoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        createRoleIfNotExists("ROLE_SUPER_ADMIN", SYSTEM_TENANT);
    }

    private void createRoleIfNotExists(String roleName, UUID tenantId) {
        if (!roleRepository.existsByNameAndTenantId(roleName, tenantId)) {
            Role role = new Role();
            role.setName(roleName);
            role.setTenantId(tenantId);
            roleRepository.save(role);
        }
    }
}
