package com.example.usermanagement.dto;

import org.junit.jupiter.api.Test;

import com.example.sharedcommon.dto.RegisterRequest;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterRequestTest {

    @Test
    void testRegisterRequestFields() {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("carol");
        register.setPassword("superpass");
        register.setEmail("carol@example.com");

        assertEquals("carol", register.getUsername());
        assertEquals("superpass", register.getPassword());
        assertEquals("carol@example.com", register.getEmail());
    }
}
