package com.dmcdoc.usermanagement.core.model;

import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.dmcdoc.usermanagement.tenant.TenantAwareEntity;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.util.UUID;

/**
 * Base class for all tenant-aware entities.
 *
 * Responsibilities:
 * - Store tenant_id
 * - Declare and apply Hibernate tenant filter
 * - Enforce tenant presence at persist time
 */
@MappedSuperclass
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntityImpl implements TenantAwareEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (tenantId == null) {
            UUID contextTenant = TenantContext.getTenantId();
            if (contextTenant != null) {
                this.tenantId = contextTenant;
            } else if (TenantContext.isBypassEnabled()) {
                this.tenantId = SystemTenant.SYSTEM_TENANT;
            } else {
                throw new IllegalStateException(
                        "TenantAwareEntity requires tenantId before persist");
            }
        }
    }
}
