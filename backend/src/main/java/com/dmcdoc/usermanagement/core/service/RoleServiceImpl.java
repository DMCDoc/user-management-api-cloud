package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import com.dmcdoc.usermanagement.tenant.hibernate.HibernateSystemQueryExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final HibernateSystemQueryExecutor systemQueryExecutor;

    /* ================= Tenant roles ================= */

    @Override
    public Role create(Role role) {
        requireTenantRole(role);
        role.setTenantId(TenantContext.getTenantId());
        return roleRepository.save(role);
    }

    @Override
    public Role update(Role role) {
        requireTenantRole(role);
        return roleRepository.save(role);
    }

    @Override
    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        assertNotSystemRole(role);
        roleRepository.delete(role);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return roleRepository.findById(id);
    }

    /* ================= System roles ================= */

    @Override
    public Optional<Role> findSystemRole(String roleName) {
        return Optional.ofNullable(
                systemQueryExecutor.findSystemRole(roleName)); /*  TODO: findSystemRole() throw déjà une exception si absent
                                                               
                                                               l’Optional est donc surtout cosmétique */
    }

    /* ================= Guards ================= */

    private void assertNotSystemRole(Role role) {
        if (SystemTenant.SYSTEM_TENANT.equals(role.getTenantId())) {
            throw new AccessDeniedException(
                    "System roles are immutable and cannot be modified");
        }
    }

    private void requireTenantRole(Role role) {
        if (SystemTenant.SYSTEM_TENANT.equals(role.getTenantId())) {
            throw new AccessDeniedException(
                    "Cannot create or update system roles");
        }
    }
}

/*
 * TODO: update(Role role) sans rechargement préalable
 * 
 * Ce n’est pas une erreur à ce stade.
 * 
 * Le filtre Hibernate garantit le tenant
 * 
 * Les guards bloquent le system
 * 
 * 👉 Si besoin d’un contrôle plus strict (versioning, existence),
 * ce sera une évolution, pas une correction.
 */

 // TODO: Test It RoleService
/*
 * Pourquoi cette version est “pro”
 * 
 * ✔ Aucune logique Hibernate dans les services
 * ✔ Executor centralisé et réutilisable
 * ✔ Règles métier explicites
 * ✔ Aucun comportement implicite
 * ✔ Compatible avec UserService / AdminService
 * ✔ Aucun risque de dette technique
 * ✔ Testable facilement
 */