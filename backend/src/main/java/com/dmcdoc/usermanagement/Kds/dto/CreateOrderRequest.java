package com.dmcdoc.usermanagement.kds.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CreateOrderRequest {

        private UUID restaurantId;
        private UUID userId;
        private List<CreateOrderItemRequest> items;
}
