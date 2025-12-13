package com.dmcdoc.usermanagement.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;

@DataJpaTest
public class IsolationTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    private UUID tenantA;

    @BeforeEach
    void setUp() {
        tenantA = UUID.randomUUID();
    }

    @Test
    void tenantIsolationIsEnforced() {

        TenantContext.setTenantId(tenantA);

        List<Restaurant> restaurants = restaurantRepository.findAll();

        assertThat(restaurants)
            .allMatch(r -> r.getTenantId().equals(tenantA));

        TenantContext.clear();
    }

    
}
