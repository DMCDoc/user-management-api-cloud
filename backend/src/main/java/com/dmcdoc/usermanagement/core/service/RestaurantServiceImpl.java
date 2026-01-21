package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository repository;

    @Override
    public List<Restaurant> all() {
        return repository.findAll(); // OK → filtré par Hibernate
    }

    @Override
    public Restaurant get(UUID id) {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("No tenant in context");
        }

        return repository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalStateException("Restaurant not found"));
    }

    @Override
    public Restaurant create(String name, String address, String metadata) {
        if (TenantContext.getTenantId() == null) {
            throw new IllegalStateException("No tenant in context");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setMetadata(metadata);

        return repository.save(restaurant);
    }
}
