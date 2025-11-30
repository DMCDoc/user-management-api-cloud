package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository repo;

    @Override
    public Tenant save(Tenant t) {
        return repo.save(t);
    }

    @Override
    public Tenant createTenant(UUID id, String name, String tenantKey, String metadata) {
        Tenant t = new Tenant();
        t.setId(id);
        t.setName(name);
        t.setTenantKey(tenantKey);
        t.setMetadata(metadata);
        t.setActive(true);
        return repo.save(t);
    }

    @Override
    public Tenant findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Tenant not found: " + id));
    }

    @Override
    public Tenant findByKey(String key) {
        return repo.findByTenantKey(key).orElseThrow(() -> new RuntimeException("Tenant not found: " + key));
    }
}
