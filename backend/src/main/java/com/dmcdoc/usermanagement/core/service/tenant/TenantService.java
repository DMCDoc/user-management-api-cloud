package com.dmcdoc.usermanagement.core.service.tenant;

import com.dmcdoc.usermanagement.core.model.Tenant;
import java.util.UUID;

public interface TenantService {

    Tenant createTenant(UUID tenantId, String name, String tenantKey, String metadata);

    // convenience overload used by controllers: create with auto-generated id
    Tenant createTenant(String tenantKey, String name, String metadata);

    Tenant findById(UUID id);

    Tenant findByKey(String tenantKey);
}
