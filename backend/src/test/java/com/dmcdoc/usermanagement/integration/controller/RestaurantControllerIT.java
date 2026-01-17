package com.dmcdoc.usermanagement.integration.controller;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.support.AbstractMultiTenantIT;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
                org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration.class
})
public class RestaurantControllerIT extends AbstractMultiTenantIT<Restaurant> {

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
        void tenantIsolationIsEnforced() throws Exception {
                repository.deleteAll();
                createEntityForTenant(tenantA);

                // Test pour tenant B : Ne doit pas voir les données de A
                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + tokenForTenant(tenantB, "ROLE_USER"))
                                .header("X-Tenant-ID", tenantB.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[?(@.tenantId == '" + tenantA + "')]").doesNotExist());

                // Test pour tenant A : Doit voir ses propres données
                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + tokenForTenant(tenantA, "ROLE_USER"))
                                .header("X-Tenant-ID", tenantA.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].tenantId").value(tenantA.toString()));
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

        @Test
        void crossTenantAccessByIdIsBlocked() throws Exception {
                UUID idA = createEntityForTenant(tenantA);

                mockMvc.perform(get("/api/restaurants/" + idA)
                                .header("Authorization", "Bearer " + tokenForTenant(tenantB, "ROLE_USER"))
                                .header("X-Tenant-ID", tenantB.toString()))
                                .andExpect(status().isForbidden()); // Doit renvoyer 403
        }

        @Test
        void tenantAdminCannotAccessOtherTenant() throws Exception {
                UUID idA = createEntityForTenant(tenantA);

                mockMvc.perform(get("/api/restaurants/" + idA)
                                .header("Authorization", "Bearer " + tokenForTenant(tenantB, "ROLE_TENANT_ADMIN"))
                                .header("X-Tenant-ID", tenantB.toString()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void superAdminCanAccessAnyTenant() throws Exception {
                UUID idA = createEntityForTenant(tenantA);

                mockMvc.perform(get("/api/restaurants/" + idA)
                                .header("Authorization", "Bearer " + superAdminToken())
                                .header("X-Tenant-ID", tenantA.toString()))
                                .andExpect(status().isOk());
        }

        @Test
        void requestWithoutTenantMustBeForbidden() throws Exception {
                String tokenWithoutTenant = jwtBuilder()
                                .withoutTenant()
                                .withRole("ROLE_USER")
                                .build();

                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + tokenWithoutTenant))
                                .andExpect(status().isForbidden());
        }

        @Test
        void invalidUuidMustBeForbiddenNotBadRequest() throws Exception {
                mockMvc.perform(get("/api/restaurants/not-a-uuid")
                                .header("Authorization", "Bearer " + tenantAdminToken())
                                .header("X-Tenant-ID", tenantA.toString()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void missingAuthenticationMustBeForbidden() throws Exception {
                mockMvc.perform(get("/api/restaurants"))
                                .andExpect(status().isForbidden()); // Ou .isForbidden() selon ta config
        }

        @Test
        void superAdminMustBypassTenantChecksEvenWithInvalidTenant() throws Exception {
                String superAdminToken = superAdminToken();

                // Le SuperAdmin franchit le filtre, mais l'ID n'existant pas, il reçoit 404
                mockMvc.perform(get("/api/restaurants/" + UUID.randomUUID())
                                .header("Authorization", "Bearer " + superAdminToken))
                                .andExpect(status().isNotFound());
        }

        @Test
        void forgedTenantInHeaderMustBeForbidden() throws Exception {
                UUID idA = createEntityForTenant(tenantA);

                mockMvc.perform(get("/api/restaurants/" + idA)
                                .header("Authorization", "Bearer " + tokenForTenant(tenantA, "ROLE_USER"))
                                .header("X-Tenant-ID", tenantB.toString())) // Tentative de fraude
                                .andExpect(status().isForbidden());
        }

        @Test
        void expiredTokenMustBeUnauthorizedEvenWithValidTenant() throws Exception {
                String expiredToken = jwtBuilder()
                                .withRole("ROLE_USER")
                                .expired()
                                .build();

                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + expiredToken))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void superAdminWithoutTenantHeaderMustStillWork() throws Exception {
                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + superAdminToken()))
                                .andExpect(status().isOk());
        }
}