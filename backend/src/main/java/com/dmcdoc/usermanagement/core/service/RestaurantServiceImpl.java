package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl {

    private final RestaurantRepository repository;

    public List<Restaurant> findAll() {
        return repository.findAll(); // filtré automatiquement
    }

    public Restaurant findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Restaurant not found"));
    }
}
