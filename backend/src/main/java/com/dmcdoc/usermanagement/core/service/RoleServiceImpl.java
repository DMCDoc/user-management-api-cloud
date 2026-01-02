package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
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

    @Override
    public Role getOrCreate(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
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
    public Role update(Role role) {
        assertNotSystemRole(role);
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

    /*
     * ============================
     * Protection centrale
     * ============================
     */
    private void assertNotSystemRole(Role role) {
        if (SystemTenant.SYSTEM_TENANT.equals(role.getTenantId())) {
            throw new AccessDeniedException(
                    "System roles are immutable and cannot be modified");
        }
    }
}
