package com.dmcdoc.usermanagement.jpa.repository;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.dmcdoc.usermanagement.Kds.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.TenantAwareRepository;

class RestaurantTenantRepositoryIT
        extends AbstractTenantAwareRepositoryIT<Restaurant> {

    @Autowired
    RestaurantRepository repository;

    @Override
    protected TenantAwareRepository<Restaurant, UUID> repository() {
        return repository;
    }

    @Override
    protected Restaurant newEntity() {
        Restaurant r = new Restaurant();
        r.setName("Test");
        return r;
    }
}
