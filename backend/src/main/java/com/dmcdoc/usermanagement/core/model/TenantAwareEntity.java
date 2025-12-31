package com.dmcdoc.usermanagement.core.model;

import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.dmcdoc.usermanagement.tenant.TenantContext;
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

// 1️⃣ Définition du filtre Hibernate
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))

// 2️⃣ Application du filtre à TOUTES les entités héritant de cette classe
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Assignation automatique du tenant
     * - STRICT par défaut
     * - Bypass uniquement pour le SYSTEM tenant
     */
    @PrePersist
    protected void onCreate() {
        if (tenantId == null) {
            if (TenantContext.isBypassEnabled()) {
                this.tenantId = SystemTenant.SYSTEM_TENANT;
            } else {
                throw new IllegalStateException(
                        "TenantAwareEntity requires tenantId before persist");
            }
        }
    }
}
