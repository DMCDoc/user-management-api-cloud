package com.dmcdoc.usermanagement.Kds.repository;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.TenantAwareRepository;

import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RestaurantRepository
                extends TenantAwareRepository<Restaurant, UUID> {
        
}
