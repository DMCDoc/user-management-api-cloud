package com.dmcdoc.usermanagement.kds.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderItemDTO {

        private UUID id;
        private UUID tenantId;
        private UUID menuItemId;
        private String name;
        private int quantity;
        private BigDecimal price;
}
