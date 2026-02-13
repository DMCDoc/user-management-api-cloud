package com.dmcdoc.usermanagement.kds.model;

import com.dmcdoc.usermanagement.core.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservation_tenant_rest", columnList = "tenant_id, restaurant_id")
})
public class Reservation extends BaseTenantEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Column(name = "when_time", nullable = false)
    private Instant when;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
