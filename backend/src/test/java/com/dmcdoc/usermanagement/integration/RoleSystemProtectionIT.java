package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.service.RoleService;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class RoleSystemProtectionIT {

    @Autowired
    RoleService roleService;

    @Autowired
    RoleRepository roleRepository;

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void cannotCreateRoleForSystemTenant() {
        TenantContext.enableBypass();
        try {
            Role role = new Role();
            role.setName("ROLE_HACK");
            role.setTenantId(SystemTenant.SYSTEM_TENANT);
            role.setActive(true);

            assertThatThrownBy(() -> roleService.create(role))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("System roles are immutable");
        } finally {
            TenantContext.disableBypass();
        }
    }

    @Test
    void cannotDeleteSystemRole() {
        TenantContext.enableBypass();
        try {
            UUID systemRoleId = roleRepository
                    .findByNameAndTenantId(
                            "ROLE_SUPER_ADMIN",
                            SystemTenant.SYSTEM_TENANT)
                    .orElseThrow()
                    .getId();

            assertThatThrownBy(() -> roleService.delete(systemRoleId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("System roles are immutable");
        } finally {
            TenantContext.disableBypass();
        }
    }
}
