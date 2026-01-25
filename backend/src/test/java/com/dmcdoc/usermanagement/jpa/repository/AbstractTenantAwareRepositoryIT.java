package com.dmcdoc.usermanagement.jpa.repository;

import com.dmcdoc.usermanagement.core.model.TenantAwareEntityImpl;
import com.dmcdoc.usermanagement.support.BaseJpaTest;
import com.dmcdoc.usermanagement.core.repository.TenantAwareRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;



public abstract class AbstractTenantAwareRepositoryIT<T extends TenantAwareEntityImpl>
        extends BaseJpaTest {

    protected abstract TenantAwareRepository<T, UUID> repository();

    protected abstract T newEntity();

    @Test
    void findByIdAndTenantId_should_enforce_isolation() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        // --- Tenant A
        TenantContext.setTenantId(tenantA);
        enableTenantFilterForCurrentTenant();

        T entity = newEntity();
        repository().saveAndFlush(entity);
        UUID id = entity.getId();

        entityManager.clear();

        // --- Tenant B
        switchTenant(tenantB);

        assertThat(repository()
                .findByIdAndTenantId(id, tenantB))
                .isEmpty();
    }
}
