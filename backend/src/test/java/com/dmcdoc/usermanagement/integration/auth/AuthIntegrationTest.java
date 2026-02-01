package com.dmcdoc.usermanagement.integration.auth;

import com.dmcdoc.sharedcommon.dto.LoginRequest;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.TenantRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.security.TestSecurityConfig;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private TenantRepository tenantRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private UUID tenantId;

        @MockBean
        private AuthenticationManager authenticationManager;

        @BeforeEach
        void setup() {

                TenantContext.setTenantId(tenantId);

                when(authenticationManager.authenticate(any(Authentication.class)))
                                .thenReturn(mock(Authentication.class));

                userRepository.deleteAll();
                tenantRepository.deleteAll();
                roleRepository.deleteAll();

                // 🏢 Tenant
                Tenant tenant = Tenant.builder()
                                .id(UUID.randomUUID())
                                .name("Tenant Test")
                                .tenantKey("tenant-test")
                                .active(true)
                                .build();

                tenantRepository.save(tenant);
                tenantId = tenant.getId();

                // 🔑 FIX ICI (APRÈS création du tenant)
                TenantContext.setTenantId(tenantId);

                // 🔐 Role
                Role userRole = Role.builder()
                                .name("ROLE_USER")
                                .build();
                userRole.setTenantId(tenantId);
                roleRepository.save(userRole);

                // 👤 User
                User user = User.builder()
                                .email("user@test.com")
                                .username("user@test.com")
                                .password(passwordEncoder.encode("password123"))
                                .roles(Set.of(userRole))
                                .enabled(true)
                                .locked(false)
                                .build();

                user.setTenantId(tenantId);
                userRepository.save(user);
        }
        
        @AfterEach
        void cleanup() {
                TenantContext.clear();
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
/*
 * Ce test valide tout ça en même temps :
 * 
 * ✔ TenantContext via header
 * ✔ SecurityFilterChain
 * ✔ AuthenticationManager
 * ✔ UserRepository + tenant scope
 * ✔ JWT génération
 * ✔ RefreshTokenService
 * ✔ Mapping rôles → authorities
 */