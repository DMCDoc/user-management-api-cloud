package com.dmcdoc.usermanagement.core.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dmcdoc.usermanagement.core.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByTenantKey(String tenantKey);
}
