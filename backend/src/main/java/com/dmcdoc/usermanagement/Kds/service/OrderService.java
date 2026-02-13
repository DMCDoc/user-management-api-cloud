package com.dmcdoc.usermanagement.kds.service;

import com.dmcdoc.usermanagement.kds.dto.CreateOrderRequest;
import com.dmcdoc.usermanagement.kds.dto.OrderDTO;
import com.dmcdoc.usermanagement.kds.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderDTO createOrder(CreateOrderRequest request);

    List<OrderDTO> getActiveOrders(UUID restaurantId);

    OrderDTO updateStatus(UUID orderId, OrderStatus status);
}

/*
 * charge les MenuItem en une requête
 * 
 * valide l’intégrité métier
 * 
 * calcule le total côté backend
 * 
 * injecte le tenantId sur Order + OrderItem
 * 
 * sauvegarde transactionnellement
 * 
 * retourne un OrderDTO
 */