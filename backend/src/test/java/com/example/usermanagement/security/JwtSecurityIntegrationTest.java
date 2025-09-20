package com.example.usermanagement.security;

import com.example.usermanagement.dto.AuthResponse;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.RefreshRequest;
import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;
import java.util.UUID;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")

class JwtSecurityIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @AfterEach
        void cleanup() {
               userRepository.deleteAll(); // ⚡ nettoie après chaque test
        }

        @Test 
        void testRegisterLoginAndRefreshFlow() throws Exception {
                // Génère un username unique avec UUID
                String uniqueUsername = "bob_" + UUID.randomUUID().toString().substring(0, 8);
                userRepository.findAll().forEach(u -> System.out.println("User test: " + u.getUsername()));

                // 1️⃣ Register user
                RegisterRequest register = new RegisterRequest();
                register.setUsername(uniqueUsername);
                register.setPassword("password123");
                register.setFullName("Bob Marley");
                register.setEmail(uniqueUsername + "@example.com");
                register.setRoles(Set.of("ROLE_USER"));

                mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(register))).andExpect(status().isCreated())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andExpect(jsonPath("$.refreshToken").exists());

                // 2️⃣ Login avec bons identifiants
               
                LoginRequest login = new LoginRequest();
                login.setUsername(uniqueUsername);
                login.setPassword("password123");

                String loginResponse = mockMvc
                                .perform(post("/users/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(login)))
                                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists())
                                .andExpect(jsonPath("$.refreshToken").exists()).andDo(print()).andReturn().getResponse()
                                .getContentAsString();

                AuthResponse tokens = objectMapper.readValue(loginResponse, AuthResponse.class);

                // 3️⃣ Login avec mauvais mot de passe
                LoginRequest badLogin = new LoginRequest();
                badLogin.setUsername(uniqueUsername);
                badLogin.setPassword("wrongpass");

                mockMvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(badLogin)))
                                .andExpect(status().isUnauthorized());

                // 4️⃣ Refresh avec refreshToken valide
                RefreshRequest refresh = new RefreshRequest();
                refresh.setRefreshToken(tokens.getRefreshToken());

                mockMvc.perform(post("/users/refresh").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refresh))).andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andExpect(jsonPath("$.refreshToken").exists());
        }
}
