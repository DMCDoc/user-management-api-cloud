package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository repo;

    @Override
    public Restaurant create(String name, String address, String metadata) {
        Restaurant r = new Restaurant();
        r.setTenantId(TenantContext.getTenantId());
        r.setName(name);
        r.setAddress(address);
        r.setMetadata(metadata);
        return repo.save(r);
    }

    @Override
    public Restaurant get(UUID id) {
        return repo.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new AccessDeniedException("Not found"));
    }

    @Override
    public List<Restaurant> all() {
        return repo.findAllByTenantId(TenantContext.getTenantId());
    }

    @Override
    public void delete(UUID id) {
        repo.delete(get(id));
    }
}
