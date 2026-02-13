package com.dmcdoc.usermanagement.kds.service;

import com.dmcdoc.usermanagement.kds.dto.CreateOrderItemRequest;
import com.dmcdoc.usermanagement.kds.dto.CreateOrderRequest;
import com.dmcdoc.usermanagement.kds.event.OrderCreatedEvent;
import com.dmcdoc.usermanagement.kds.event.OrderEventPublisher;
import com.dmcdoc.usermanagement.kds.event.OrderStatusUpdatedEvent;
import com.dmcdoc.usermanagement.kds.model.*;
import com.dmcdoc.usermanagement.kds.repository.MenuItemRepository;
import com.dmcdoc.usermanagement.kds.repository.OrderRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID tenantId;
    private UUID menuItemId;

    @BeforeEach
    void setup() {
        tenantId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();

        TenantContext.setTenantId(tenantId);
    }

    @Test
    void shouldCreateOrderAndPublishEvent() {

        // GIVEN
        CreateOrderItemRequest itemRequest = CreateOrderItemRequest.builder()
                .menuItemId(menuItemId)
                .quantity(2)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .items(List.of(itemRequest))
                .build();

        MenuItem menuItem = MenuItem.builder()
                .id(menuItemId)
                .name("Pizza")
                .price(BigDecimal.valueOf(10))
                .available(true)
                .build();

        when(menuItemRepository.findByIdIn(any()))
                .thenReturn(List.of(menuItem));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(UUID.randomUUID());
                    return order;
                });

        // WHEN
        var result = orderService.createOrder(request);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualByComparingTo("20");

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventPublisher, times(1))
                .publishOrderCreated(any(OrderCreatedEvent.class));
    }

    @Test
    void shouldUpdateStatusAndPublishEvent() {

        // GIVEN
        UUID orderId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatus.CREATED)
                .total(BigDecimal.TEN)
                .build();

        order.setTenantId(tenantId);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        // WHEN
        var result = orderService.updateStatus(orderId, OrderStatus.PREPARING);

        // THEN
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PREPARING);

        verify(orderEventPublisher, times(1))
                .publishOrderStatusUpdated(any(OrderStatusUpdatedEvent.class));
    }
}
/*
 * Ce que ces tests garantissent
 * 
 * ✔ Calcul total correct
 * ✔ Sauvegarde DB appelée
 * ✔ Event création publié
 * ✔ Event update publié
 * ✔ Logique métier isolée
 */