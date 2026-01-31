package com.dmcdoc.usermanagement.tenant.hibernate;

/*
Avec cette version :

✔ Une seule classe connaît Hibernate
✔ Une seule classe gère le filtre tenant
✔ Un seul cache partagé
✔ Tous les services utilisent la même règle
✔ Tests plus simples
✔ Aucun bricolage

👉 C’est une fondation SaaS propre.
*/

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class HibernateSystemQueryExecutor {

    private final RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Cache des rôles système (immutables).
     */
    private final Map<String, Role> systemRoleCache = new ConcurrentHashMap<>();

    /* ================= Generic API ================= */

    public <T> T runAsSystem(Supplier<T> action) {
        Session session = entityManager.unwrap(Session.class);
        boolean filterWasEnabled = session.getEnabledFilter("tenantFilter") != null;

        try {
            if (filterWasEnabled) {
                session.disableFilter("tenantFilter");
            }
            return action.get();
        } finally {
            if (filterWasEnabled) {
                session.enableFilter("tenantFilter")
                        .setParameter("tenantId", TenantContext.getTenantId());
            }
        }
    }

    public void runAsSystem(Runnable action) {
        runAsSystem(() -> {
            action.run();
            return null;
        });
    }

    /* ================= System roles ================= */

    public Role findSystemRole(String roleName) {
        return systemRoleCache.computeIfAbsent(roleName, name -> runAsSystem(() -> roleRepository
                .findByNameAndTenantId(name, SystemTenant.SYSTEM_TENANT)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role not found: " + name))));
    }

    /**
     * Utile pour tests ou rechargement contrôlé.
     */
    public void clearCache() {
        systemRoleCache.clear();
    }
}
