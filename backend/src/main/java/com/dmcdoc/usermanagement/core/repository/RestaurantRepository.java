package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Optional<Restaurant> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Restaurant> findAllByTenantId(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
