package com.dmcdoc.usermanagement.dto;


import org.junit.jupiter.api.Test;

import com.dmcdoc.sharedcommon.dto.LoginRequest;

import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestTest {

    @Test
    void testLoginRequestFields() {
        // LoginRequest is now a record with constructor (email, password)
        LoginRequest login = new LoginRequest("bob@example.com", "secure123");

        assertEquals("bob@example.com", login.email());
        assertEquals("secure123", login.password());
    }
}
