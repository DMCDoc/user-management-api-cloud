package com.dmcdoc.usermanagement.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // ✅ status(), content(), jsonPath()



@SpringBootTest
@AutoConfigureMockMvc


class SecurityExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenNoAuth_thenReturns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/dummy/only-auth")) // endpoint protégé
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/dummy/only-auth"));
    }

    @Test
    @WithMockUser(roles = { "USER" }) // ✅ connecté mais pas ADMIN
    void whenForbidden_thenReturns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/dummy/admin")) // endpoint accessible uniquement ROLE_ADMIN
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/dummy/admin"));
    }



    @Test @WithMockUser(roles = { "ADMIN" }) // admin => accès OK
    void whenAdmin_thenReturns200() throws Exception {
        mockMvc.perform(get("/api/dummy/admin"))
        .andExpect(status().isOk())
                .andExpect(content().string("Admin zone"));
    }

}
