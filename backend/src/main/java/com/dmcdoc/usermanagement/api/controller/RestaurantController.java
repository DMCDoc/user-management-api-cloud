package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.usermanagement.Kds.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.core.model.Restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    @GetMapping
    public List<Restaurant> getAll() {
        return restaurantRepository.findAll();
    }

    @GetMapping("/{id}")
    public Restaurant getById(@PathVariable UUID id) {
        return restaurantRepository.findById(id).orElseThrow();
    }
}
