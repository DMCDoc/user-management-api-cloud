package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.TenantAwareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface TenantAwareRepository<T extends TenantAwareEntity, ID extends Serializable>
        extends JpaRepository<T, ID> {

    @Override
    @Deprecated
    Optional<T> findById(ID id);

    @Override
    @Deprecated
    List<T> findAll();

    @Override
    @Deprecated
    void deleteById(ID id);

    // ✅ Méthodes autorisées
    Optional<T> findByIdAndTenantId(ID id, UUID tenantId);

    List<T> findAllByTenantId(UUID tenantId);

    void deleteByIdAndTenantId(ID id, UUID tenantId);
}
