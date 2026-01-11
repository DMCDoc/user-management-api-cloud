package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.TenantAwareEntityImpl;
import jakarta.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.io.Serializable;

public class TenantAwareRepositoryImpl<T extends TenantAwareEntityImpl, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements TenantAwareRepository<T, ID> {

    public TenantAwareRepositoryImpl(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager
    ) {
        super(entityInformation, entityManager);
    }
}


