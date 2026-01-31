package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.config.jpa.HibernateTenantFilterConfig;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
@Import({
        HibernateTenantFilterConfig.class,
        com.dmcdoc.usermanagement.security.TestSecurityConfig.class
})
class UserServiceIT {

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private UUID tenantA;
    private UUID tenantB;

    @BeforeEach
    void setUp() {
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();

        TenantContext.setTenantId(tenantA);
        enableTenantFilter();

        // Ensure system role exists (idempotent — TestRoleInitializer already creates
        // it on startup)
        if (!roleRepository.existsByNameAndTenantId("ROLE_TENANT_ADMIN", SystemTenant.SYSTEM_TENANT)) {
            Role role = new Role();
            role.setName("ROLE_TENANT_ADMIN");
            role.setTenantId(SystemTenant.SYSTEM_TENANT);
            role.setActive(true);

            roleRepository.save(role);
        }

        entityManager.flush();
        entityManager.clear();

        // 🔥 MANQUAIT ICI
        enableTenantFilter();
    }

    @Test
    void service_should_not_return_user_of_other_tenant() {
        /* ===== Tenant A ===== */
        TenantContext.setTenantId(tenantA);
        enableTenantFilter();

        User user = userService.createAdminForTenant(
                tenantA,
                "admin@tenantA.com",
                "encoded-password",
                "Admin",
                "TenantA");

        UUID userId = user.getId();

        entityManager.flush();
        entityManager.clear();

        /* IMPORTANT */

        enableTenantFilter(); // <- OBLIGATOIRE après un clear()

        /* ===== Tenant B ===== */
        switchTenant(tenantB);

        assertThatThrownBy(() -> userService.deleteAccountById(userId, tenantB))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /* ================= Helpers ================= */

    private void enableTenantFilter() {
        entityManager.unwrap(Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", TenantContext.getTenantId());
    }

    private void switchTenant(UUID tenantId) {
        entityManager.clear();
        TenantContext.setTenantId(tenantId);
        enableTenantFilter();

    }

    @AfterEach
    void tearDown() {
        try {
            entityManager.unwrap(Session.class)
                    .disableFilter("tenantFilter");
        } catch (Exception ignored) {
        }
        TenantContext.clear();
    }
}
