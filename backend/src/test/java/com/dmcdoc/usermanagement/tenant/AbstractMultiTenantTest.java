package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
public abstract class AbstractMultiTenantTest {

    @Autowired
    protected JwtService jwtService;

    protected UUID tenantA = UUID.randomUUID();
    protected UUID tenantB = UUID.randomUUID();

    protected String tokenForTenant(UUID tenantId, String role) {
        User user = new User();
        user.setUsername("test-" + tenantId);
        user.setTenantId(tenantId);

        Role r = new Role();
        r.setName(role);
        user.setRoles(Set.of(r));

        return jwtService.generateToken(user);
    }

    protected String superAdminToken() {
        User user = new User();
        user.setUsername("superadmin");

        Role r = new Role();
        r.setName("ROLE_SUPER_ADMIN");
        user.setRoles(Set.of(r));

        return jwtService.generateToken(user);
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }
}
