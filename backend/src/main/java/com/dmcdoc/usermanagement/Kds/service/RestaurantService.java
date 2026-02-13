package com.dmcdoc.usermanagement.Kds.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;

import java.util.List;
import java.util.UUID;


public interface RestaurantService {

    List<Restaurant> all();

    Restaurant get(UUID id);

    Restaurant create(String name, String address, String metadata);
}
