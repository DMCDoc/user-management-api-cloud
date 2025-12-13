package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.repository.TenantRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public Tenant createTenant(UUID tenantId, String name, String tenantKey, String metadata) {

        if (tenantRepository.existsByTenantKey(tenantKey)) {
            throw new IllegalArgumentException("Tenant key already exists");
        }

        Tenant t = new Tenant();
        t.setId(tenantId);
        t.setName(name);
        t.setTenantKey(tenantKey);
        t.setMetadata(metadata);
        return tenantRepository.save(t);
    }

    @Override
    public Tenant createTenant(String tenantKey, String name, String metadata) {
        UUID id = UUID.randomUUID();
        return createTenant(id, name, tenantKey, metadata);
    }

    @Override
    public Tenant findById(UUID id) {
        UUID currentTenantId = TenantContext.getTenantId();
        if (currentTenantId == null) {
            // Super admin access
            return tenantRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        }
        // Tenant-scoped access: only allow reading own tenant
        if (!currentTenantId.equals(id)) {
            throw new AccessDeniedException("Cannot access another tenant");
        }
        return tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    @Override
    public Tenant findByKey(String tenantKey) {
        return tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }
}
