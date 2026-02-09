package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.TenantAwareEntityImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface TenantAwareRepository<T extends TenantAwareEntityImpl, ID extends Serializable>
        extends JpaRepository<T, ID> {

    Optional<T> findByIdAndTenantId(ID id, UUID tenantId);

    boolean existsById(ID id);

    boolean existsByIdAndTenantId(ID id, UUID tenantId);
}
