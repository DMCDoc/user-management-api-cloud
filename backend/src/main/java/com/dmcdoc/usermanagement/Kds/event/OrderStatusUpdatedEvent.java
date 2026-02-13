package com.dmcdoc.usermanagement.kds.event;

import com.dmcdoc.usermanagement.kds.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Événement publié lorsqu'une commande change de statut (PREPARING, READY,
 * CANCELLED…).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdatedEvent {

    private UUID orderId;
    private UUID tenantId;
    private OrderStatus status;
}
