package com.dmcdoc.usermanagement.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * Active dynamiquement le filtre Hibernate tenantFilter
 * AVANT toute requête JPA
 */
@Component
public class HibernateTenantFilterEnabler {

    @PersistenceContext
    private EntityManager entityManager;

    public void enableTenantFilter() {

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }

        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);

        if (session.getEnabledFilter("tenantFilter") == null) {
            session.enableFilter("tenantFilter")
                    .setParameter("tenantId", tenantId);
        }
    }
}
