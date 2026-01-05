package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RestaurantRepository
        extends TenantAwareRepository<Restaurant, UUID> {
}
