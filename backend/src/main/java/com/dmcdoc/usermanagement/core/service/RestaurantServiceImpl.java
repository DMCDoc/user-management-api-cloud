package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<Restaurant> all() {
        // 1. Si Super Admin (Bypass) -> On renvoie TOUT (findAll)
        if (TenantContext.isBypassEnabled()) {
            return restaurantRepository.findAll();
        }

        // 2. Sinon, on exige un tenant et on filtre
        if (!TenantContext.isResolved()) {
            throw new AccessDeniedException("Tenant context required");
        }
        return restaurantRepository.findAllByTenantId(TenantContext.getTenantId());
    }

    @Override
    public Restaurant get(UUID id) {
        // 1. Si Super Admin (Bypass) -> Accès direct par ID
        if (TenantContext.isBypassEnabled()) {
            return restaurantRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        }

        // 2. Sinon, on doit vérifier que le restaurant appartient au tenant courant
        UUID tenantId = TenantContext.getTenantIdRequired(); // Lève une exception si null

        return restaurantRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AccessDeniedException("Access denied to this restaurant"));
        // Note: Ici on renvoie AccessDenied (403) plutôt que Not Found pour la sécurité
    }

    @Override
    public Restaurant create(String name, String address, String metadata) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setMetadata(metadata);

        // On force le tenant ID du contexte
        restaurant.setTenantId(TenantContext.getTenantIdRequired());
        restaurant.setActive(true);

        return restaurantRepository.save(restaurant);
    }
}