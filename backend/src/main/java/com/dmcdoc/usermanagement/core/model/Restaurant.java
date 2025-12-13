package com.dmcdoc.usermanagement.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "restaurants", uniqueConstraints = @UniqueConstraint(columnNames = { "id",
        "tenant_id" }), indexes = @Index(name = "idx_restaurant_tenant", columnList = "tenant_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends TenantAwareEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
