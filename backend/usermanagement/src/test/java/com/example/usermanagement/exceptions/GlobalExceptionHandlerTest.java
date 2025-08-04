package com.example.usermanagement.exceptions;

import com.example.usermanagement.controller.DummyController;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.JWTFilter;
import com.example.usermanagement.security.JWTUtils;
import com.example.usermanagement.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest// <===// CONTROLLER TO TEST
@Import({ GlobalExceptionHandler.class, DummyController.class, JWTFilter.class, JWTUtils.class }) // <===// ESSENTIELLE
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JWTFilter jwtFilter;

    @MockBean
    private UserService userService;

    @MockBean
    private JWTUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;




    @Test
    void whenUserExists_thenReturns409() throws Exception {
        mockMvc.perform(get("/api/dummy/fail/user-exists")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Test: User already exists"))
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void whenUserNotFound_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/dummy/fail/user-not-found")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Test: User not found"))
                .andExpect(content().contentType("application/json"));
    }



    @Test
    void whenIllegalState_thenReturns409() throws Exception {
        mockMvc.perform(get("/api/dummy/illegal-state")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Conflict error"));
    }

   @Test
   void whenUnhandledException_thenReturns500() throws Exception {
       mockMvc.perform(get("/api/dummy/unhandled")).andExpect(status().isInternalServerError())
               .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
               .andExpect(content().contentType("application/json"));
   }
    
   @Test
   void whenIllegalArgument_thenReturns400() throws Exception {
       // Utilisez le chemin corrigé
       mockMvc.perform(get("/api/dummy/illegal-arg")).andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Invalid input"));
   }
}
