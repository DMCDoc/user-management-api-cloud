package com.dmcdoc.usermanagement.kds.repository;

import com.dmcdoc.usermanagement.kds.model.Order;
import com.dmcdoc.usermanagement.kds.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    List<Order> findByRestaurantIdAndStatusIn(UUID restaurantId, List<OrderStatus> statuses);
}
