package com.dmcdoc.usermanagement.tenant;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantSecurity")
public class TenantSecurity {

    public boolean isSameTenant(UUID resourceTenantId) {
        return TenantContext.getTenantId().equals(resourceTenantId);
    }
}
