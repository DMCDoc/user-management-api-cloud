package com.dmcdoc.usermanagement.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_tenantkey", columnList = "tenant_key")
})
public class Tenant {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "tenant_key", nullable = false, unique = true)
    private String tenantKey; // ex: "restaurant-123"

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private boolean active = true;
}
