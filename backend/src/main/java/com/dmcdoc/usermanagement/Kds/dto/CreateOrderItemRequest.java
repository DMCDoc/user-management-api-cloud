package com.dmcdoc.usermanagement.kds.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreateOrderItemRequest {

        private UUID menuItemId;
        private int quantity;
}
