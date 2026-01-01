package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.AbstractMultiTenantTest;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration.class
})
public class test extends AbstractMultiTenantTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository repository;

    @Override
    protected UUID createEntityForTenant(UUID tenantId) {
        TenantContext.enableBypass();
        try {
            Restaurant restaurant = new Restaurant();
            restaurant.setName("Restaurant " + tenantId);
            restaurant.setTenantId(tenantId);
            restaurant.setActive(true);
            return repository.save(restaurant).getId();
        } finally {
            TenantContext.disableBypass();
        }
    }

    @Test
    void superAdminCanAccessAllTenants() throws Exception {
        repository.deleteAll();
        createEntityForTenant(tenantA);
        createEntityForTenant(tenantB);

        mockMvc.perform(get("/api/restaurants")
                .header("Authorization", "Bearer " + superAdminToken()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    assertThat(body).contains(tenantA.toString());
                    assertThat(body).contains(tenantB.toString());
                });
    }
}