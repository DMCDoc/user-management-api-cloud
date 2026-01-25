package com.dmcdoc.usermanagement.jpa.repository;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.support.BaseJpaTest;
import com.dmcdoc.usermanagement.support.JpaTestConfig;
import com.dmcdoc.usermanagement.tenant.TenantContext;


@DataJpaTest
@ActiveProfiles("test")
@Import(JpaTestConfig.class)
class RestaurantRepositoryIT extends BaseJpaTest {

    static final UUID TENANT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    static final UUID TENANT_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Autowired
    private RestaurantRepository repository;



    @Test
    void persist_without_tenant_should_fail() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("NoTenant");

assertThatThrownBy(() -> repository.saveAndFlush(restaurant))
    .isInstanceOf(InvalidDataAccessApiUsageException.class)
    .hasRootCauseInstanceOf(IllegalStateException.class)
    .hasMessageContaining("tenantId");

    }

    @Test
    void persist_with_tenant_should_work() {
        TenantContext.setTenantId(TENANT_A);
        enableTenantFilterForCurrentTenant();

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Resto A");

        restaurant = repository.saveAndFlush(restaurant);

        assertThat(restaurant.getTenantId()).isEqualTo(TENANT_A);
    }

    @Test
    void should_isolate_restaurants_by_tenant() {
        // --- Tenant A
        TenantContext.setTenantId(TENANT_A);
        enableTenantFilterForCurrentTenant();

        Restaurant a1 = new Restaurant();
        a1.setName("A1");
        repository.saveAndFlush(a1);

        // IMPORTANT : vider le cache JPA
        entityManager.clear();

        // --- Tenant B
        TenantContext.setTenantId(TENANT_B);
        enableTenantFilterForCurrentTenant();

        Restaurant b1 = new Restaurant();
        b1.setName("B1");
        repository.saveAndFlush(b1);

        // --- Back to Tenant A
        TenantContext.setTenantId(TENANT_A);
        enableTenantFilterForCurrentTenant();

        assertThat(repository.findAll())
                .extracting(Restaurant::getName)
                .containsExactly("A1");
    }

    @Test
    void findById_other_tenant_should_return_empty() {

        // --- Tenant A
        TenantContext.setTenantId(TENANT_A);
        enableTenantFilterForCurrentTenant();

        Restaurant restaurant = new Restaurant();
        restaurant.setName("A1");
        restaurant = repository.saveAndFlush(restaurant);

        UUID id = restaurant.getId();

        // IMPORTANT : vider le cache JPA
        entityManager.clear();

        // --- Tenant B
        switchTenant(TENANT_B);

        Optional<Restaurant> result = repository.findByIdAndTenantId(id, TenantContext.getTenantId());

        assertThat(result).isEmpty();
    }
    

 
}
