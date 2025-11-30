package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository repo;

    @Override
    public Restaurant save(Restaurant r) {
        return repo.save(r);
    }

    @Override
    public Restaurant createRestaurant(UUID tenantId, String name, String address, String metadata) {
        Restaurant r = new Restaurant();
        r.setTenantId(tenantId);
        r.setName(name);
        r.setAddress(address);
        r.setMetadata(metadata);
        return repo.save(r);
    }
}
