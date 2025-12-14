package com.dmcdoc.usermanagement.tenant.hibernate;

import com.dmcdoc.usermanagement.tenant.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl
    implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_SCHEMA = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        if (!TenantContext.isResolved()) {
            return DEFAULT_SCHEMA;
        }
        return TenantContext.getTenantId().toString();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
