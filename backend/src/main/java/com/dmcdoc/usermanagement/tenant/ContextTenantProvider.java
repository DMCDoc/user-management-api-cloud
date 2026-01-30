package com.dmcdoc.usermanagement.tenant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implémentation hors web :
 * utilisée pour les tests, batchs, CLI, migrations.
 */
@Component
@ConditionalOnNotWebApplication
public class ContextTenantProvider implements CurrentTenantProvider {

    @Override
    public UUID getCurrentTenant() {
        return TenantContext.getTenantId();
    }
}
