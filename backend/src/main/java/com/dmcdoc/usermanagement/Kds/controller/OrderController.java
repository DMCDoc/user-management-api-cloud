package com.dmcdoc.usermanagement.kds.controller;

import com.dmcdoc.usermanagement.kds.dto.CreateOrderRequest;
import com.dmcdoc.usermanagement.kds.dto.OrderDTO;
import com.dmcdoc.usermanagement.kds.model.OrderStatus;
import com.dmcdoc.usermanagement.kds.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        OrderDTO created = orderService.createOrder(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/restaurant/{restaurantId}/active")
    public ResponseEntity<List<OrderDTO>> getActiveOrders(@PathVariable UUID restaurantId) {
        List<OrderDTO> orders = orderService.getActiveOrders(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {
        OrderDTO updated = orderService.updateStatus(orderId, status);
        return ResponseEntity.ok(updated);
    }
}
/*
 * Points clés :
 * 
 * Utilise @RequestBody pour créer une commande.
 * 
 * Filtre par restaurant pour récupérer les commandes actives.
 * 
 * @PatchMapping pour updateStatus avec @RequestParam pour le status.
 * 
 * Tout est typé DTO et reste multi-tenant safe grâce au service.
 */