package com.dmcdoc.usermanagement.tenant;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantCurrentProviderImpl implements TenantCurrentProvider {

    @Override
    public UUID getTenantId() {
        if (TenantContext.isBypassEnabled()) {
            return null;
        }
        return TenantContext.getTenantId();
    }

    @Override
    public boolean isBypass() {
        return TenantContext.isBypassEnabled();
    }

    @Override
    public boolean isSuperAdmin() {
        return TenantContext.isBypassEnabled();
    }
}
