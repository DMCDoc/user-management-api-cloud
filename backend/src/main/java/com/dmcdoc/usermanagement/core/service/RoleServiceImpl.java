package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getOrCreate(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setId(UUID.randomUUID());
                    r.setName(roleName);
                    return roleRepository.save(r);
                });
    }

    @Override
    public Role create(Role role) {
        assertNotSystemRole(role);
        return roleRepository.save(role);
    }

    @Override
    public void delete(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        assertNotSystemRole(role);
        roleRepository.delete(role);
    }

    /**
     * 🔒 Règle métier centrale :
     * un rôle du SYSTEM_TENANT est IMMUTABLE
     */
    private void assertNotSystemRole(Role role) {
        if (SystemTenant.SYSTEM_TENANT.equals(role.getTenantId())) {
            throw new IllegalStateException(
                    "System roles are immutable and cannot be created, modified or deleted");
        }
    }
}
