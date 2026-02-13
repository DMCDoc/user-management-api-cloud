package com.dmcdoc.usermanagement.kds.mapper;

import com.dmcdoc.usermanagement.kds.dto.OrderDTO;
import com.dmcdoc.usermanagement.kds.dto.OrderItemDTO;
import com.dmcdoc.usermanagement.kds.model.Order;
import com.dmcdoc.usermanagement.kds.model.OrderItem;

import java.util.stream.Collectors;

public final class OrderMapper {

        private OrderMapper() {
        }

        public static OrderDTO toDTO(Order order) {
                if (order == null)
                        return null;

                return OrderDTO.builder()
                                .id(order.getId())
                                .tenantId(order.getTenantId())
                                .restaurantId(order.getRestaurantId())
                                .userId(order.getUserId())
                                .status(order.getStatus())
                                .total(order.getTotal())
                                .createdAt(order.getCreatedAt())
                                .items(order.getItems().stream()
                                                .map(OrderMapper::toDTO)
                                                .collect(Collectors.toList()))
                                .build();
        }

        public static OrderItemDTO toDTO(OrderItem item) {
                if (item == null)
                        return null;

                return OrderItemDTO.builder()
                                .id(item.getId())
                                .tenantId(item.getTenantId())
                                .menuItemId(item.getMenuItemId())
                                .name(item.getName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build();
        }
}
