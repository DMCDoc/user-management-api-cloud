package com.dmcdoc.usermanagement.core.model;

import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.dmcdoc.usermanagement.tenant.TenantAwareEntity;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.util.UUID;

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
            if (TenantContext.isBypassEnabled()) {
                this.tenantId = SystemTenant.SYSTEM_TENANT;
            } else {
                throw new IllegalStateException(
                        "TenantAwareEntity requires tenantId before persist");
            }
        }
    }
}
