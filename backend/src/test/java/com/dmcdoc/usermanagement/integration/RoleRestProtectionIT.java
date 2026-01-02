package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.AbstractMultiTenantTest;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")

public class RoleRestProtectionIT extends AbstractMultiTenantTest {

    @Autowired
    private MockMvc mockMvc; // ✅ MANQUANT

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected UUID createEntityForTenant(UUID tenantId) {
        return null; // OK ici, non utilisé
    }

    @Test
    void cannotCreateSystemRoleViaRest() throws Exception {

        Role role = new Role();
        role.setName("ROLE_HACK");
        role.setTenantId(SystemTenant.SYSTEM_TENANT);

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role))
                .header("Authorization", "Bearer " + superAdminToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotDeleteSystemRoleViaRest() throws Exception {

        UUID systemRoleId = roleRepository
                .findByNameAndTenantId("ROLE_SUPER_ADMIN", SystemTenant.SYSTEM_TENANT)
                .orElseThrow()
                .getId();

        mockMvc.perform(delete("/api/roles/{id}", systemRoleId)
                .header("Authorization", "Bearer " + superAdminToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRoleIdMustReturn400() throws Exception {

        mockMvc.perform(delete("/api/roles/not-a-uuid")
                .header("Authorization", "Bearer " + superAdminToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletingUnknownRoleReturns404() throws Exception {

        mockMvc.perform(delete("/api/roles/{id}", UUID.randomUUID())
                .header("Authorization", "Bearer " + superAdminToken()))
                .andExpect(status().isNotFound());
    }
}
