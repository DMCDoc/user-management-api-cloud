package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import java.util.UUID;

public interface RestaurantRepository
        extends TenantAwareRepository<Restaurant, UUID> {

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
