package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
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
        // Le filtre Hibernate s’applique automatiquement
        return repository.findAll();
    }

    @Override
    public Restaurant get(UUID id) {
        // Si le tenant ne correspond pas → entité filtrée → Optional.empty()
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
    }

    @Override
    public Restaurant create(String name, String address, String metadata) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setMetadata(metadata);

        return repository.save(restaurant);
    }
}
