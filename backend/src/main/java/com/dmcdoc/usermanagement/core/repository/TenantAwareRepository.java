package com.dmcdoc.usermanagement.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface TenantAwareRepository<T, ID>
        extends JpaRepository<T, ID> {

    Optional<T> findByIdAndTenantId(ID id, UUID tenantId);
}
