package com.dmcdoc.usermanagement.core.service;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

public abstract class TenantAwareService<T, ID> {

    protected abstract UUID getCurrentTenant();

    protected abstract Optional<T> findByIdAndTenant(ID id, UUID tenantId);

    protected T getOrFail(ID id) {
        UUID tenant = getCurrentTenant();
        return findByIdAndTenant(id, tenant)
                .orElseThrow(EntityNotFoundException::new);
    }
}
