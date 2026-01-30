package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.config.jpa.HibernateTenantFilterConfig;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@Import({
        HibernateTenantFilterConfig.class,
        com.dmcdoc.usermanagement.security.TestSecurityConfig.class
})
class UserServiceIT {

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    private UUID tenantA;
    private UUID tenantB;

    @BeforeEach
    void setUp() {
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
        TenantContext.clear();
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
        TenantContext.setTenantId(tenantId);
        enableTenantFilter();
        entityManager.clear();
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
