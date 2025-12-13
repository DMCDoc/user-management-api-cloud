package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    Restaurant create(String name, String address, String metadata);

    Restaurant get(UUID id);

    List<Restaurant> all();

    void delete(UUID id);
}
