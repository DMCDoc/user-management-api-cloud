package com.example.usermanagement.exceptions;

import com.example.usermanagement.controller.DummyController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private DummyController dummyController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dummyController).setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void whenUserExists_thenReturns409() throws Exception {
        mockMvc.perform(get("/api/dummy/fail/user-exists")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Test: User already exists"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void whenUserNotFound_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/dummy/fail/user-not-found")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Test: User not found"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void whenIllegalState_thenReturns409() throws Exception {
        mockMvc.perform(get("/api/dummy/illegal-state")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Conflict error"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void whenUnhandledException_thenReturns500() throws Exception {
        mockMvc.perform(get("/api/dummy/unhandled")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void whenIllegalArgument_thenReturns400() throws Exception {
        mockMvc.perform(get("/api/dummy/illegal-arg")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}
