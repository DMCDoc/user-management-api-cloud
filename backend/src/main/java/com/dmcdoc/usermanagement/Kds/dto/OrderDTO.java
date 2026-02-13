package com.dmcdoc.usermanagement.kds.dto;

import com.dmcdoc.usermanagement.kds.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderDTO {

        private UUID id;
        private UUID tenantId;
        private UUID restaurantId;
        private UUID userId;
        private OrderStatus status;
        private BigDecimal total;
        private List<OrderItemDTO> items;
        private Instant createdAt;
}
/*
 * Points clés :
 * 
 * DTO pour exposer les commandes via l'API.
 * 
 * Contient les IDs nécessaires pour le multi-tenant et la relation avec restaurant/user.
 * 
 * Inclut une liste d'items pour détailler la commande.
 */