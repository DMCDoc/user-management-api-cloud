package com.dmcdoc.usermanagement.support;

import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

/**
 * Base class for all JPA / Repository tests.
 *
 * This class guarantees:
 * - A valid tenant context is always present
 * - Hibernate tenant filters are enabled
 * - Tests fail if tenant isolation is broken
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(com.dmcdoc.usermanagement.config.jpa.HibernateTenantFilterConfig.class)
public abstract class BaseJpaTest {

    /**
     * Static UUID used for all repository tests.
     * Must be stable and predictable.
     */
    protected static final UUID TEST_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * Initialize tenant context before each test.
     */
    @BeforeEach
    void setUpTenantContext() {
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    /**
     * Clean tenant context after each test.
     */
    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }
}
