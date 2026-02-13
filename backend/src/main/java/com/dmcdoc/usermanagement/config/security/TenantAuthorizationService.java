package com.dmcdoc.usermanagement.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.dmcdoc.usermanagement.kds.repository.RestaurantRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantAuthorizationService {

    private final RestaurantRepository restaurantRepository;

    public boolean canAccessRestaurant(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId).isPresent();
    }
}
