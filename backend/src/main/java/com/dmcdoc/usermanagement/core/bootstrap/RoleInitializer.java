package com.dmcdoc.usermanagement.core.bootstrap;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "test", "dev", "prod" })
public class RoleInitializer {

    public static final UUID SYSTEM_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        createIfMissing("ROLE_SUPER_ADMIN", SYSTEM_TENANT);
        createIfMissing("ROLE_TENANT_ADMIN", SYSTEM_TENANT);
        createIfMissing("ROLE_USER", SYSTEM_TENANT);
    }

    private void createIfMissing(String name, UUID tenantId) {
        if (!roleRepository.existsByNameAndTenantId(name, tenantId)) {
            Role role = new Role();
            role.setName(name);
            role.setTenantId(tenantId);
            role.setActive(true);
            roleRepository.save(role);
        }
    }
}
