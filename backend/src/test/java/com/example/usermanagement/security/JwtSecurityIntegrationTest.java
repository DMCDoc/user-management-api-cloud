package com.example.usermanagement.security;

import com.example.usermanagement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") // utilisation du
                                                              // fichier
                                                              // application-test.properties
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtils jwtUtils;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Crée un utilisateur factice pour le test
        User testUser = new User();
        testUser.setUsername("testuser");

        // Génère un token valide via JWTUtils injecté par Spring
        jwtToken = jwtUtils.generateToken(testUser);
    }

    @Test
    void protectedEndpoint_ShouldReturn200_WhenValidToken() throws Exception {
        mockMvc.perform(get("/users/profile") // ton endpoint protégé réel
                .header("Authorization", "Bearer " + jwtToken)).andExpect(status().isOk()); // 200
                                                                                            // OK
                                                                                            // attendu
    }

    @Test
    void protectedEndpoint_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/users/profile")).andExpect(status().isUnauthorized()); // 401
                                                                                     // attendu
                                                                                     // si
                                                                                     // pas
                                                                                     // de
                                                                                     // token
    }

}
