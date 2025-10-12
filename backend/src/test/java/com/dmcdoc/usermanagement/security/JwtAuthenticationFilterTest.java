package com.dmcdoc.usermanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

    @BeforeEach
    void setup() {
        System.setProperty("APP_ENV", "test");
    }

    /*@BeforeEach
    void setup() {
        System.setProperty("APP_ENV", "test");
    }*/

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealth_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk()); // ✅
                                                                             // doit
                                                                             // passer
                                                                             // sans
                                                                             // JWT
    }

    @Test
    void actuatorInfo_ShouldBeAccessibleWithoutAuth_WhenExcluded() throws Exception {
        mockMvc.perform(get("/actuator/info")).andExpect(status().isOk()); // ✅
                                                                           // passe
                                                                           // si
                                                                           // /actuator/**
                                                                           // est
                                                                           // exclu
    }

    @Test
    void protectedEndpoint_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/dummy")) // endpoint réel et protégé
                .andDo(print())
                .andExpect(status().isUnauthorized()); // doit échouer sans JWT

    }

}
