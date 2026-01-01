package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getOrCreate(String roleName) {

        // 🔒 CAS SPÉCIAL : rôle système
        if ("ROLE_SUPER_ADMIN".equals(roleName)) {
            return roleRepository.findByNameAndTenantId(
                    roleName,
                    SystemTenant.SYSTEM_TENANT)
                    .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN must be pre-initialized"));
        }

        // ⚠️ Cas legacy : sans tenant → interdit en multi-tenant
        throw new IllegalStateException(
                "getOrCreate(roleName) without tenant is forbidden for non system roles");
    }
}
