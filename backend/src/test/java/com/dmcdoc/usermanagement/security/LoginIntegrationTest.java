package com.dmcdoc.usermanagement.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

@SpringBootTest @AutoConfigureMockMvc
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test @Transactional(propagation = Propagation.NOT_SUPPORTED) // 🚀 pas de
                                                                  // transaction
                                                                  // partagée
    void testRegisterAndLoginSeparateTransactions() throws Exception {
        String username = "bob_" + UUID.randomUUID().toString().substring(0, 6);
        String email = username + "@example.com";

        // --- 1) Register ---
        String registerJson = """
                {
                  "fullName": "Bob Marley",
                  "username": "%s",
                  "password": "password123",
                  "email": "%s",
                  "roles": null
                }
                """.formatted(username, email);

        mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(registerJson))
                .andExpect(status().isCreated());

        // --- 2) Login ---
        String loginJson = """
                {
                  "username": "%s",
                  "password": "password123"
                }
                """.formatted(username);

        mockMvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
