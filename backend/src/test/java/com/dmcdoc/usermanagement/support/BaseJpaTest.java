package com.dmcdoc.usermanagement.support;

import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.UUID;

import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all JPA / Repository integration tests.
 *
 * Contract:
 * - NO tenant is set implicitly
 * - NO Hibernate filter is enabled implicitly
 * - Each test is responsible for defining its tenant context
 * - Tenant bypass is always disabled by default
 *
 * This guarantees:
 * - Deterministic tenant behavior
 * - No cross-test pollution
 * - Repository tests truly validate tenant isolation
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({
        com.dmcdoc.usermanagement.config.jpa.HibernateTenantFilterConfig.class,
        JpaTestConfig.class
})
public abstract class BaseJpaTest {

    @PersistenceContext
    protected EntityManager entityManager;

    @BeforeEach
    void beforeEach() {
        // Always start with a clean tenant context
        TenantContext.clear();

        // Explicitly disable tenant bypass to avoid silent persistence
        TenantContext.disableBypass();
    }

    /**
     * Enables the Hibernate tenant filter for the given tenant.
     *
     * This MUST be called explicitly by tests that expect tenant isolation.
     */
    protected void enableTenantFilterForCurrentTenant() {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("No tenantId in TenantContext");
        }

        entityManager
                .unwrap(org.hibernate.Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", tenantId);
    }

    protected void switchTenant(UUID tenantId) {
        TenantContext.setTenantId(tenantId);
        enableTenantFilterForCurrentTenant();
        entityManager.clear();
    }

    @AfterEach
    void afterEach() {
        // Disable tenant filter if it was enabled
        try {
            entityManager.unwrap(Session.class)
                    .disableFilter("tenantFilter");
        } catch (Exception ignored) {
            // Filter may not have been enabled — this is expected in some tests
        }

        // Always clear tenant context after each test
        TenantContext.clear();
    }
}
