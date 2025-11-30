package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Tenant;

import java.util.UUID;

/**
 * Service interface for tenant operations.
 * Implementations should provide persistence logic (see `TenantServiceImpl`).
 */
public interface TenantService {
    Tenant save(Tenant t);

    Tenant createTenant(UUID id, String name, String tenantKey, String metadata);

    Tenant findById(UUID id);

    Tenant findByKey(String key);
}
