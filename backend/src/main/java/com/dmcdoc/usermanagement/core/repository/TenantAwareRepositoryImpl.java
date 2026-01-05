package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.TenantAwareEntityImpl;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.io.Serializable;

public class TenantAwareRepositoryImpl<T extends TenantAwareEntityImpl, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements TenantAwareRepository<T, ID> {

    public TenantAwareRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
    }
}
