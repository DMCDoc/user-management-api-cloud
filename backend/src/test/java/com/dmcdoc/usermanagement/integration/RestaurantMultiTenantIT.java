/*
Cas testé

tenant A crée un restaurant

tenant B ne peut pas le voir

tenant A le voit

super admin voit tout
*/

package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.AbstractMultiTenantTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestaurantMultiTenantIT extends AbstractMultiTenantTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository repository;

//Test tenant isolation

    @Test
    void tenantIsolationIsEnforced() throws Exception {

            // 🔹 create restaurant for tenant A
            Restaurant r = new Restaurant();
            r.setTenantId(tenantA);
            r.setName("Tenant A Restaurant");
            repository.save(r);

            // 🔒 tenant B → FORBIDDEN / NOT FOUND
            mockMvc.perform(get("/api/restaurants")
                            .header("Authorization",
                                            "Bearer " + tokenForTenant(tenantB, "ROLE_USER"))
                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                                            .doesNotContain("Tenant A Restaurant"));

            // ✅ tenant A → OK
            mockMvc.perform(get("/api/restaurants")
                            .header("Authorization",
                                            "Bearer " + tokenForTenant(tenantA, "ROLE_USER"))
                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                                            .contains("Tenant A Restaurant"));
    }
    
    /*Test super admin bypass global (prouve que :

    TenantContext.enableBypass() fonctionne

    Hibernate Filter estcorrectement désactivé)*/
    
    @Test
    void superAdminCanAccessAllTenants() throws Exception {

            Restaurant r1 = new Restaurant();
            r1.setTenantId(tenantA);
            r1.setName("A");
            repository.save(r1);

            Restaurant r2 = new Restaurant();
            r2.setTenantId(tenantB);
            r2.setName("B");
            repository.save(r2);

            mockMvc.perform(get("/api/restaurants")
                            .header("Authorization",
                                            "Bearer " + superAdminToken()))
                            .andExpect(status().isOk())
                            .andExpect(result -> {
                                    String body = result.getResponse().getContentAsString();
                                    assertThat(body).contains("A");
                                    assertThat(body).contains("B");
                            });
    }

    /*
     * 🧨 Test sécurité – attaque cross-tenant
     * couvre :
     * 
     * @PreAuthorize
     * 
     * repository findByIdAndTenantId
     * 
     * filtre SQL
     */

     @Test
    void crossTenantAccessByIdIsBlocked() throws Exception {

            Restaurant r = new Restaurant();
            r.setTenantId(tenantA);
            r.setName("Private A");
            repository.save(r);

            mockMvc.perform(get("/api/restaurants/" + r.getId())
                            .header("Authorization",
                                            "Bearer " + tokenForTenant(tenantB, "ROLE_USER")))
                            .andExpect(status().isForbidden());
    }

    @Test
    void tenantAdminCannotAccessOtherTenant() throws Exception {

            Restaurant r = new Restaurant();
            r.setTenantId(tenantA);
            r.setName("A");
            repository.save(r);

            mockMvc.perform(get("/api/restaurants/" + r.getId())
                            .header("Authorization",
                                            "Bearer " + tokenForTenant(tenantB, "ROLE_TENANT_ADMIN")))
                            .andExpect(status().isForbidden());
    }

    @Test
    void superAdminCanAccessAnyTenant() throws Exception {

            Restaurant r = new Restaurant();
            r.setTenantId(tenantA);
            r.setName("A");
            repository.save(r);

            mockMvc.perform(get("/api/restaurants/" + r.getId())
                            .header("Authorization",
                                            "Bearer " + superAdminToken()))
                            .andExpect(status().isOk());
    }

}
