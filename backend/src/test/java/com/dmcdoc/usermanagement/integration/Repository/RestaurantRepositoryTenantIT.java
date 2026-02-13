package com.dmcdoc.usermanagement.integration.Repository;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.dmcdoc.usermanagement.Kds.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.support.BaseIntegrationTest;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class RestaurantRepositoryTenantIT extends BaseIntegrationTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void existsByIdAndTenantId_shouldReturnTrue_forSameTenant() {

        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test Resto");
        restaurant.setTenantId(tenantId);

        restaurant = restaurantRepository.save(restaurant);

        boolean exists = restaurantRepository
                .existsByIdAndTenantId(restaurant.getId(), tenantId);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByIdAndTenantId_shouldReturnFalse_forOtherTenant() {

        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        TenantContext.setTenantId(tenantA);

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Tenant A Resto");
        restaurant.setTenantId(tenantA);

        restaurant = restaurantRepository.save(restaurant);

        boolean exists = restaurantRepository
                .existsByIdAndTenantId(restaurant.getId(), tenantB);

        assertThat(exists).isFalse();
    }
}
