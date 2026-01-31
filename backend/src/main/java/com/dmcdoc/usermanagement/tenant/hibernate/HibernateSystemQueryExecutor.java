package com.dmcdoc.usermanagement.tenant.hibernate;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Exécute une action Hibernate en désactivant temporairement
 * le filtre tenant pour accéder aux données système.
 *
 * Utilisé uniquement pour les entités globales (SYSTEM tenant).
 */
@Component
@RequiredArgsConstructor
public class HibernateSystemQueryExecutor {

    private final EntityManager entityManager;
    private final RoleRepository roleRepository;

    private final Map<String, Role> systemRoleCache = new ConcurrentHashMap<>();

    public Role findSystemRole(String roleName) {

        // Cache first (system roles are immutable)
        Role cached = systemRoleCache.get(roleName);
        if (cached != null) {
            return cached;
        }

        Session session = entityManager.unwrap(Session.class);
        boolean tenantFilterWasEnabled = session.getEnabledFilter("tenantFilter") != null;

        try {
            if (tenantFilterWasEnabled) {
                session.disableFilter("tenantFilter");
            }

            Role role = roleRepository
                    .findByNameAndTenantId(roleName, SystemTenant.SYSTEM_TENANT)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Role not found: " + roleName));

            systemRoleCache.put(roleName, role);
            return role;

        } finally {
            if (tenantFilterWasEnabled) {
                session.enableFilter("tenantFilter")
                        .setParameter("tenantId", TenantContext.getTenantId());
            }
        }
    }
}
