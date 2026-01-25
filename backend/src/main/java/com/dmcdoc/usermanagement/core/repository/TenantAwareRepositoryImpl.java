package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.TenantAwareEntityImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public class TenantAwareRepositoryImpl<T extends TenantAwareEntityImpl, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements TenantAwareRepository<T, ID> {

    private final EntityManager entityManager;
            private final Class<T> domainClass;

    public TenantAwareRepositoryImpl(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager
    ) {
        super(entityInformation, entityManager);

        System.out.println(
                ">>> TenantAwareRepositoryImpl loaded for "
                        + entityInformation.getJavaType().getSimpleName());

        this.entityManager = entityManager;
        this.domainClass = entityInformation.getJavaType();
    }

      @Override
    public Optional<T> findByIdAndTenantId(ID id, UUID tenantId) {

        String jpql = """
            select e
            from %s e
            where e.id = :id
              and e.tenantId = :tenantId
        """.formatted(domainClass.getName());

        TypedQuery<T> query = entityManager.createQuery(jpql, domainClass);
        query.setParameter("id", id);
        query.setParameter("tenantId", tenantId);

        return query.getResultStream().findFirst();
    }
}


