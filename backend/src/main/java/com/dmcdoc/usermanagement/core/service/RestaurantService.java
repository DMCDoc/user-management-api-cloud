package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import java.util.UUID;

public interface RestaurantService {
    Restaurant save(Restaurant r);

    Restaurant createRestaurant(UUID id, UUID tenantId, String name, String address, String metadata);
}
