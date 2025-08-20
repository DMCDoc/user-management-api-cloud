package com.example.usermanagement.dto;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserDtoTest {

    @Test
    void testUserDtoFields() {
        UserResponse user = new UserResponse(null, null, null);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");

        assertEquals("john_doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("John Doe", user.getFullName());
    }
}
