package com.dmcdoc.usermanagement.kds.repository;

import com.dmcdoc.usermanagement.kds.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByRestaurantIdAndAvailableTrue(UUID restaurantId);

    List<MenuItem> findByIdIn(List<UUID> ids);
}
