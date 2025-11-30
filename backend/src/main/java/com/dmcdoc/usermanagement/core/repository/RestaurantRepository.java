package com.dmcdoc.usermanagement.core.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dmcdoc.usermanagement.core.model.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    // Optionnel : recherche par tenant
    java.util.List<Restaurant> findByTenantId(UUID tenantId);
}
