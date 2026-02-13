package com.dmcdoc.usermanagement.kds.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Événement publié lorsqu'une nouvelle commande est créée.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID tenantId;
    private UUID restaurantId;
    private UUID userId;
    private BigDecimal total;

    // Liste des items de la commande
    private List<OrderItemEvent> items;

    /**
     * DTO interne pour représenter chaque item de la commande.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private UUID menuItemId;
        private String name;
        private int quantity;
        private BigDecimal price;
    }
}
