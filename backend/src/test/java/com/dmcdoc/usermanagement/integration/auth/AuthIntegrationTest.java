package com.dmcdoc.usermanagement.integration.auth;

import com.dmcdoc.sharedcommon.dto.LoginRequest;
import com.dmcdoc.usermanagement.security.TestSecurityConfig;
import com.dmcdoc.usermanagement.support.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthenticationManager authenticationManager;

        private UUID tenantId;

        @BeforeEach
        void setup() {
                // Mock de l'AuthenticationManager
                when(authenticationManager.authenticate(any(Authentication.class)))
                                .thenReturn(mock(Authentication.class));

                // Création standard via BaseIntegrationTest
                TestEntities entities = createTenantWithUser(
                                "Tenant Test",
                                "user@test.com",
                                "password123",
                                "ROLE_USER");

                tenantId = entities.tenant.getId();
        }

        @Test
        void login_OK_withTenantAndRoles() throws Exception {

                LoginRequest request = new LoginRequest(
                                "user@test.com",
                                "password123");

                mockMvc.perform(post("/api/auth/login")
                                .header("X-Tenant-ID", tenantId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                                .andExpect(jsonPath("$.email").value("user@test.com"));
        }
}
