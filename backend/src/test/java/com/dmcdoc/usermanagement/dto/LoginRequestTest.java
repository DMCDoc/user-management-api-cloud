package com.dmcdoc.usermanagement.dto;


import org.junit.jupiter.api.Test;

import com.dmcdoc.sharedcommon.dto.LoginRequest;

import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestTest {

    @Test
    void testLoginRequestFields() {
        LoginRequest login = new LoginRequest();
        login.setUsername("bob");
        login.setPassword("secure123");

        assertEquals("bob", login.getUsername());
        assertEquals("secure123", login.getPassword());
    }
}
