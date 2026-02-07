package com.dmcdoc.usermanagement.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BaseIntegrationTestIT extends BaseIntegrationTest {

    @Test
    void createTenantWithUser_activatesTenantContextByDefault() {
        TestEntities e = createTenantWithUser("tenant-it-1", "it.user@test.com");
        assertNotNull(e);
        assertNotNull(e.tenant);
        assertEquals(e.tenant.getId(), com.dmcdoc.usermanagement.tenant.TenantContext.getTenantId());
    }

    @Test
    void createTenantWithUser_doesNotActivateWhenFlagFalse() {
        // Ensure clean context
        clearTenant();

        TestEntities e = createTenantWithUser("tenant-it-2", "it.user2@test.com", "password123", "ROLE_USER", false);
        assertNotNull(e);
        assertNotNull(e.tenant);
        assertNull(com.dmcdoc.usermanagement.tenant.TenantContext.getTenantId());
    }
}
