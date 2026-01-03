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
public class Restaurant extends BaseTenantEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private boolean active = true;

    public boolean isInactive() {
        return !this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private String address;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
