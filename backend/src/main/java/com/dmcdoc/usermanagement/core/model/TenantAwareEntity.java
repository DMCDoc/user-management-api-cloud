/*TenantAwareEntity HARDENED

🎯 Objectifs atteints :

tenant obligatoire

tenant immuable

compatible tenant_id aujourd’hui

compatible schema / db par tenant demain

sans TenantContext */

package com.dmcdoc.usermanagement.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantAwareEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Garantit qu'aucune entité tenant-aware
     * ne peut être persistée sans tenant
     */
    @PrePersist
    protected void onCreate() {
        if (tenantId == null) {
            throw new IllegalStateException(
                    "TenantAwareEntity requires tenantId before persist");
        }
    }

    /**
     * Empêche toute tentative de modification du tenant
     */
    @PreUpdate
    protected void onUpdate() {
        if (tenantId == null) {
            throw new IllegalStateException(
                    "tenantId cannot be null on update");
        }
    }
}
