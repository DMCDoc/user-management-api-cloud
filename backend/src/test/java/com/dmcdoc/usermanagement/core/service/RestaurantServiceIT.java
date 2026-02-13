package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.config.jpa.HibernateTenantFilterConfig;
import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.kds.service.RestaurantService;
import com.dmcdoc.usermanagement.kds.service.RestaurantServiceImpl;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {
        RestaurantServiceImpl.class,
        HibernateTenantFilterConfig.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@EntityScan(
        "com.dmcdoc.usermanagement.core"
)
@EnableJpaRepositories(basePackages = {
        "com.dmcdoc.usermanagement.core.repository"
})
@ActiveProfiles("test")
@Transactional
class RestaurantServiceIT {

    @Autowired
    private RestaurantService service;

    @PersistenceContext
    private EntityManager entityManager;

    private UUID tenantA;
    private UUID tenantB;

    @BeforeEach
    void setUp() {
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
        TenantContext.clear();
        TenantContext.disableBypass();
    }

    @Test
    void service_should_not_return_entity_of_other_tenant() {
        // Tenant A
        TenantContext.setTenantId(tenantA);
        enableTenantFilter();

        Restaurant r = service.create("A", null, null);
        UUID id = r.getId();

        entityManager.flush();
        entityManager.clear();

        // Tenant B
        switchTenant(tenantB);

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private void enableTenantFilter() {
        entityManager.unwrap(Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", TenantContext.getTenantId());
    }

    private void switchTenant(UUID tenantId) {
        TenantContext.setTenantId(tenantId);
        enableTenantFilter();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        try {
            entityManager.unwrap(Session.class).disableFilter("tenantFilter");
        } catch (Exception ignored) {
        }
        TenantContext.clear();
    }
}
