package com.dmcdoc.usermanagement.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
class SecurityJsonIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return structured JSON for 401 Unauthorized")
    void shouldReturnJsonFor401() throws Exception {
        // On appelle une URL "bidon" mais sécurisée (api/protected/nowhere)
        // Comme elle commence par /api, elle n'est pas dans le permitAll des ressources
        // statiques
        // Comme elle n'a pas de contrôleur, si Security la laisse passer -> 404 (Not
        // Found)
        // Si Security fait son job -> 401 (Unauthorized)

        mockMvc.perform(get("/api/users/secure-test-endpoint-that-does-not-exist")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized()) // On veut 401
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "USER") // Simule un utilisateur identifié sans rôle ADMIN
    @DisplayName("Should return structured JSON for 403 Forbidden")
    void shouldReturnJsonFor403() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/admin/dashboard"));
    }
}