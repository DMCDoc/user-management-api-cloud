package com.dmcdoc.usermanagement.support;

import com.dmcdoc.usermanagement.support.security.JwtTestTokenBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

public abstract class AbstractMultiTenantIT<T> {

    protected UUID tenantA;
    protected UUID tenantB;

    @BeforeEach
    void setupTenants() {
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
    }

    @AfterEach
    void cleanup() {
        // hook si besoin
    }

    protected abstract UUID createEntityForTenant(UUID tenantId);

    protected JwtTestTokenBuilder jwtBuilder() {
        return new JwtTestTokenBuilder();
    }

    protected String tokenForTenant(UUID tenantId, String role) {
        return jwtBuilder()
                .withTenant(tenantId)
                .withRole(role)
                .build();
    }

    protected String tenantAdminToken() {
        return tokenForTenant(tenantA, "ROLE_TENANT_ADMIN");
    }

    protected String superAdminToken() {
        return jwtBuilder()
                .withRole("ROLE_SUPER_ADMIN")
                .build();
    }
}
