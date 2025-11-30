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
    public Tenant createTenant(String tenantKey, String name, String metadata) {
        Tenant t = new Tenant();
        t.setTenantKey(tenantKey);
        t.setName(name);
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
