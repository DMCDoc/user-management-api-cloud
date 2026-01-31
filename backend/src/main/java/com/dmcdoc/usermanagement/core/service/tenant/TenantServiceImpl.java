package com.dmcdoc.usermanagement.core.service.tenant;

import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
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
        t.setActive(true);

        return tenantRepository.save(t);
    }

    @Override
    public Tenant createTenant(String tenantKey, String name, String metadata) {
        return createTenant(UUID.randomUUID(), name, tenantKey, metadata);
    }

    @Override
    public Tenant findById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    @Override
    public Tenant findByKey(String tenantKey) {
        return tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }
}
/*
 * ✔ Aucun couplage à TenantContext
 * ✔ Utilisable en :
 * 
 * bootstrap
 * 
 * batch
 * 
 * tests
 * 
 * super admin
 * ✔ Sécurité claire et déplaçable
 * ✔ Aucune dette technique
 */