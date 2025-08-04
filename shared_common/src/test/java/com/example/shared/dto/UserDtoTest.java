package com.example.shared.dto;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserDtoTest {

    @Test
    void testUserDtoFields() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        assertEquals(1L, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
    }
}
