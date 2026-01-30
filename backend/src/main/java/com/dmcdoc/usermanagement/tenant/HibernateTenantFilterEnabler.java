package com.dmcdoc.usermanagement.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HibernateTenantFilterEnabler {

    @PersistenceContext
    private EntityManager entityManager;

    public void enableTenantFilter(UUID tenantId) {
        if (tenantId == null) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        if (session.getEnabledFilter("tenantFilter") == null) {
            session.enableFilter("tenantFilter")
                    .setParameter("tenantId", tenantId);
        }
    }

    public void disableTenantFilter() {
        Session session = entityManager.unwrap(Session.class);
        if (session.getEnabledFilter("tenantFilter") != null) {
            session.disableFilter("tenantFilter");
        }
    }
}
