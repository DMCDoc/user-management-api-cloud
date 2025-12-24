package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.CreateRestaurantRequest;
import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public Restaurant create(@RequestBody CreateRestaurantRequest req) {
        return restaurantService.create(
                req.getName(),
                req.getAddress(),
                req.getMetadata());
    }

    @GetMapping
    public List<Restaurant> list() {
        return restaurantService.all();
    }

    @PreAuthorize("@tenantAuth.canAccessRestaurant(#id)")
    @GetMapping("/{id}")
    public Restaurant get(@PathVariable("id") UUID id) {
        return restaurantService.get(id);
    }

    @PreAuthorize("@tenantAuth.canAccessRestaurant(#id)")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        restaurantService.delete(id);
    }
}
