package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.AbstractMultiTenantTest;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
public class RestaurantMultiTenantIT extends AbstractMultiTenantTest {


        @BeforeEach
void enableBypass() {
    TenantContext.enableBypass();
}

@AfterEach
void disableBypass() {
    TenantContext.disableBypass();
}


        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private RestaurantRepository repository;

        @Override
        protected UUID createEntityForTenant(UUID tenantId) {
                Restaurant restaurant = new Restaurant();
                restaurant.setName("Restaurant " + tenantId);
                restaurant.setTenantId(tenantId);
                // Correction du nom de la variable de 'restaurantRepository' vers 'repository'
                return repository.save(restaurant).getId();
        }

        @Test
        void tenantIsolationIsEnforced() throws Exception {
                repository.deleteAll();

                Restaurant r = new Restaurant();
                r.setTenantId(tenantA);
                r.setName("Tenant A Restaurant");
                repository.save(r);

                // Test pour tenant B
                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + tokenForTenant(tenantB, "ROLE_USER"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[?(@.name == 'Tenant A Restaurant')]").doesNotExist());

                // Test pour tenant A
                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + tokenForTenant(tenantA, "ROLE_USER"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[?(@.name == 'Tenant A Restaurant')]").exists());
        }

        @Test
        void superAdminCanAccessAllTenants() throws Exception {
                repository.deleteAll();

                Restaurant r1 = new Restaurant();
                r1.setTenantId(tenantA);
                r1.setName("A");
                repository.save(r1);

                Restaurant r2 = new Restaurant();
                r2.setTenantId(tenantB);
                r2.setName("B");
                repository.save(r2);

                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + superAdminToken()))
                                .andExpect(status().isOk())
                                .andExpect(result -> {
                                        String body = result.getResponse().getContentAsString();
                                        assertThat(body).contains("A");
                                        assertThat(body).contains("B");
                                });
        }

        @Test
        void crossTenantAccessByIdIsBlocked() throws Exception {
                Restaurant r = new Restaurant();
                r.setTenantId(tenantA);
                r.setName("Private A");
                repository.save(r);

                mockMvc.perform(get("/api/restaurants/" + r.getId())
                                .header("Authorization", "Bearer " + tokenForTenant(tenantB, "ROLE_USER")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void tenantAdminCannotAccessOtherTenant() throws Exception {
                Restaurant r = new Restaurant();
                r.setTenantId(tenantA);
                r.setName("A");
                repository.save(r);

                mockMvc.perform(get("/api/restaurants/" + r.getId())
                                .header("Authorization", "Bearer " + tokenForTenant(tenantB, "ROLE_TENANT_ADMIN")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void superAdminCanAccessAnyTenant() throws Exception {
                Restaurant r = new Restaurant();
                r.setTenantId(tenantA);
                r.setName("A");
                repository.save(r);

                mockMvc.perform(get("/api/restaurants/" + r.getId())
                                .header("Authorization", "Bearer " + superAdminToken()))
                                .andExpect(status().isOk());
        }

        @Test
        void requestWithoutTenantMustBeForbidden() throws Exception {
                // Ici on utilise le builder car c'est un test de sécurité sur JWT malformé
                String tokenWithoutTenant = jwtBuilder()
                                .withoutTenant()
                                .withRole("TENANT_ADMIN")
                                .build();

                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + tokenWithoutTenant))
                                .andExpect(status().isForbidden());
        }

        @Test
        void nullTenantContextMustBeForbidden() throws Exception {
                mockMvc.perform(get("/api/restaurants")
                                .header("Authorization", "Bearer " + tenantAdminToken())
                                .header("X-Tenant-ID", ""))
                                .andExpect(status().isForbidden());
        }

        @Test
        void invalidUuidMustBeForbiddenNotBadRequest() throws Exception {
                mockMvc.perform(get("/api/restaurants/not-a-uuid")
                                .header("Authorization", "Bearer " + tenantAdminToken()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void crossTenantAccessMustBeForbidden() throws Exception {
                UUID otherTenantRestaurantId = createEntityForTenant(tenantA);

                mockMvc.perform(get("/api/restaurants/{id}", otherTenantRestaurantId)
                                .header("Authorization", "Bearer " + tenantAdminToken()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void missingAuthenticationMustBeForbidden() throws Exception {
                mockMvc.perform(get("/api/restaurants"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void superAdminMustBypassTenantChecksEvenWithInvalidTenant() throws Exception {
                String superAdminToken = superAdminTokenWithoutTenant();

                // On teste une URL qui n'appartient à personne
                mockMvc.perform(get("/api/restaurants/" + UUID.randomUUID())
                                .header("Authorization", "Bearer " + superAdminToken))
                                .andExpect(status().isNotFound()); // Le super admin passe le filtre, mais ne trouve pas
                                                                                                                                                                                     // la donnée
        }
        
        @Test
        void forgedTenantInHeaderMustBeForbidden() throws Exception {
                UUID restaurantId = createEntityForTenant(tenantA);

                mockMvc.perform(get("/api/restaurants/{id}", restaurantId)
                                .header("Authorization", "Bearer " + tokenForTenant(tenantA, "ROLE_USER"))
                                .header("X-Tenant-ID", tenantB.toString()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void expiredTokenMustBeUnauthorizedEvenWithValidTenant() throws Exception {
                UUID restaurantId = createEntityForTenant(tenantA);

                String expiredToken = jwtBuilder()
                                .withRole("ROLE_USER")
                                .expired()
                                .build();

                mockMvc.perform(get("/api/restaurants/{id}", restaurantId)
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