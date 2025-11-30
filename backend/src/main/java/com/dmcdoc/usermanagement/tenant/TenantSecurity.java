package com.dmcdoc.usermanagement.tenant;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantSecurity")
public class TenantSecurity {

    public boolean isSameTenant(UUID resourceTenantId) {
        String current = TenantContext.getCurrentTenant();
        if (current == null) return false;
        return resourceTenantId.equals(UUID.fromString(current));
    }
}
