package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public Restaurant createRestaurant(UUID tenantId, String name, String address, String metadata) {
        Restaurant r = new Restaurant();
        r.setId(UUID.randomUUID());
        r.setTenantId(tenantId);
        r.setName(name);
        r.setAddress(address);
        r.setMetadata(metadata);
        r.setActive(true);

        return restaurantRepository.save(r);
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant findByIdAndTenant(UUID restaurantId, UUID tenantId) {
        return restaurantRepository.findByIdAndTenantId(restaurantId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found for this tenant"));
    }

    @Override
    public List<Restaurant> findAllByTenant(UUID tenantId) {
        return restaurantRepository.findAllByTenantId(tenantId);
    }

    @Override
    public void deleteByIdAndTenant(UUID restaurantId, UUID tenantId) {
        Restaurant r = findByIdAndTenant(restaurantId, tenantId);
        restaurantRepository.delete(r);
    }
}
