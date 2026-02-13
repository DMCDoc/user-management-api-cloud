package com.dmcdoc.usermanagement.kds.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.kds.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
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
        /*
         * Le filtre Hibernate tenantFilter est déjà actif.
         * Seules les entités du tenant courant sont retournées.
         */
        return repository.findAll();
    }

    @Override
    public Restaurant get(UUID id) {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("No tenant in context");
        }

        /*
         * Double protection :
         * - Filtre Hibernate (niveau persistence)
         * - Clause explicite tenantId (niveau métier)
         */
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
    }

    @Override
    public Restaurant create(String name, String address, String metadata) {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("No tenant in context");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setMetadata(metadata);

        /*
         * POINT CRITIQUE :
         * Le tenant DOIT être fixé à la création.
         */
        restaurant.setTenantId(tenantId);

        return repository.save(restaurant);
    }
}
