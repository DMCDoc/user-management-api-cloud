package com.example.usermanagement.dto;


import org.junit.jupiter.api.Test;

import com.example.sharedcommon.dto.UserResponse;

import static org.junit.jupiter.api.Assertions.*;

public class UserDtoTest {

    @Test
    void testUserDtoFields() {
        UserResponse user = new UserResponse();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");

        assertEquals("john_doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("John Doe", user.getFullName());
    }
}
