package com.dmcdoc.usermanagement.tenant;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantContextResolver {

    public UUID resolveRequired() {
        return TenantContext.getTenantIdRequired();
    }

    public UUID resolveNullable() {
        return TenantContext.getTenantId();
    }
}
