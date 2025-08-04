package com.example.shared.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserResponseTest {

    @Test
    void testUserResponseFields() {
        UserDto response = new UserDto();
        response.setId(2L);
        response.setUsername("dave");
        response.setEmail("dave@example.com");

        assertEquals(2L, response.getId());
        assertEquals("dave", response.getUsername());
        assertEquals("dave@example.com", response.getEmail());
    }
}
