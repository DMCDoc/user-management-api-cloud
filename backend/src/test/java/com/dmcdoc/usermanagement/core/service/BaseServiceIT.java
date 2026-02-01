package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.config.jpa.HibernateTenantFilterConfig;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(HibernateTenantFilterConfig.class)
public abstract class BaseServiceIT {

    @PersistenceContext
    protected EntityManager entityManager;

    protected void enableTenantFilterForCurrentTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("No tenantId in TenantContext");
        }
        entityManager.unwrap(Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", tenantId);
    }

    protected void switchTenant(UUID tenantId) {
        TenantContext.setTenantId(tenantId);
        enableTenantFilterForCurrentTenant();
        entityManager.clear();
    }
}
