/*TenantAwareEntity HARDENED

🎯 Objectifs atteints :

tenant obligatoire

tenant immuable

compatible tenant_id aujourd’hui

compatible schema / db par tenant demain

sans TenantContext */

package com.dmcdoc.usermanagement.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (tenantId == null) {
            throw new IllegalStateException(
                    "TenantAwareEntity requires tenantId before persist");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (tenantId == null) {
            throw new IllegalStateException(
                    "tenantId cannot be null on update");
        }
    }
}
