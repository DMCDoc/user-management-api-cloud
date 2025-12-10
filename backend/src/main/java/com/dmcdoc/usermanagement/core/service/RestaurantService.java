package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    Restaurant createRestaurant(UUID tenantId, String name, String address, String metadata);

    Restaurant save(Restaurant restaurant);

    Restaurant findByIdAndTenant(UUID restaurantId, UUID tenantId);

    List<Restaurant> findAllByTenant(UUID tenantId);

    void deleteByIdAndTenant(UUID restaurantId, UUID tenantId);
}
