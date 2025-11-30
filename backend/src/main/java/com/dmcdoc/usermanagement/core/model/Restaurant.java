package com.dmcdoc.usermanagement.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurant_tenant", columnList = "tenant_id")
})
public class Restaurant {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID tenantId;

    @Column(name = "restaurant_name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
