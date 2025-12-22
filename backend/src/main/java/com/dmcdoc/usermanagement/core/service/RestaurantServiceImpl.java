package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.TenantCurrentProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository repository;
    private final TenantCurrentProvider tenantProvider;

    @Override
    public Restaurant create(String name, String address, String metadata) {

        UUID tenantId = tenantProvider.getTenantId();

        if (repository.existsByTenantIdAndName(tenantId, name)) {
            throw new IllegalArgumentException("Restaurant already exists");
        }

        Restaurant r = new Restaurant();
        r.setTenantId(tenantId);
        r.setName(name);
        r.setAddress(address);
        r.setMetadata(metadata);
        r.setActive(true);

        return repository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public Restaurant get(UUID id) {
        UUID tenantId = tenantProvider.getTenantId();
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> all() {
        UUID tenantId = tenantProvider.getTenantId();
        return repository.findAllByTenantId(tenantId);
    }

    @Override
    public void delete(UUID id) {
        UUID tenantId = tenantProvider.getTenantId();
        repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        repository.deleteByIdAndTenantId(id, tenantId);
    }
}
