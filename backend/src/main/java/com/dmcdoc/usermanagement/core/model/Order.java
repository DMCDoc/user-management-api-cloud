package com.dmcdoc.usermanagement.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_tenant_rest", columnList = "tenant_id, restaurant_id")
})
public class Order {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "UUID", nullable = false)
    private UUID tenantId;

    @Column(name = "restaurant_id", columnDefinition = "UUID", nullable = false)
    private UUID restaurantId;

    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;

    @Column(name = "status")
    private String status;

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
