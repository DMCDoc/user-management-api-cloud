package com.dmcdoc.usermanagement.kds.service;

import com.dmcdoc.usermanagement.kds.dto.CreateOrderItemRequest;
import com.dmcdoc.usermanagement.kds.dto.CreateOrderRequest;
import com.dmcdoc.usermanagement.kds.dto.OrderDTO;
import com.dmcdoc.usermanagement.kds.mapper.OrderMapper;
import com.dmcdoc.usermanagement.kds.model.*;
import com.dmcdoc.usermanagement.kds.repository.MenuItemRepository;
import com.dmcdoc.usermanagement.kds.repository.OrderRepository;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.CREATED,
            OrderStatus.PREPARING,
            OrderStatus.READY);

    @Override
    public OrderDTO createOrder(CreateOrderRequest request) {

        UUID tenantId = TenantContext.getTenantIdRequired();

        // Récupération des IDs des menu items
        List<UUID> menuIds = request.getItems().stream()
                .map(CreateOrderItemRequest::getMenuItemId)
                .toList();

        List<MenuItem> menuItems = menuItemRepository.findByIdIn(menuIds);

        if (menuItems.size() != menuIds.size()) {
            throw new IllegalArgumentException("One or more menu items not found");
        }

        Map<UUID, MenuItem> menuMap = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getId, m -> m));

        Order order = Order.builder()
                .restaurantId(request.getRestaurantId())
                .userId(request.getUserId())
                .status(OrderStatus.CREATED)
                .total(BigDecimal.ZERO)
                .build();

        order.setTenantId(tenantId);

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemReq : request.getItems()) {

            MenuItem menuItem = menuMap.get(itemReq.getMenuItemId());

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException("Menu item not available");
            }

            BigDecimal lineTotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            total = total.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .menuItemId(menuItem.getId())
                    .name(menuItem.getName())
                    .quantity(itemReq.getQuantity())
                    .price(menuItem.getPrice())
                    .build();

            orderItem.setTenantId(tenantId);
            order.addItem(orderItem);
        }

        order.setTotal(total);

        Order saved = orderRepository.save(order);

        return OrderMapper.toDTO(saved);
    }

    @Override
    public List<OrderDTO> getActiveOrders(UUID restaurantId) {
        List<Order> orders = orderRepository.findByRestaurantIdAndStatusIn(restaurantId, ACTIVE_STATUSES);
        return orders.stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    @Override
    public OrderDTO updateStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update status of a cancelled order");
        }

        order.setStatus(status);
        Order saved = orderRepository.save(order);
        return OrderMapper.toDTO(saved);
    }

}
