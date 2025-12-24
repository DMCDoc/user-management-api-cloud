package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.TenantCurrentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantAuth")
@RequiredArgsConstructor
public class TenantAuthorizationService {

    private final RestaurantRepository restaurantRepository;
    private final TenantCurrentProvider tenantProvider;

    /**
     * Vérifie si l'utilisateur peut accéder à un restaurant
     */
    public boolean canAccessRestaurant(UUID restaurantId) {

        if (tenantProvider.isSuperAdmin()) {
            return true;
        }

        UUID tenantId = tenantProvider.getTenantId();

        return restaurantRepository.existsByIdAndTenantId(restaurantId, tenantId);
    }
}
