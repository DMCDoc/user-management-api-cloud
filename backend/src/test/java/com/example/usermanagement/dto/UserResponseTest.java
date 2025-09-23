package com.example.usermanagement.dto;


import org.junit.jupiter.api.Test;

import com.example.sharedcommon.dto.UserResponse;

import static org.junit.jupiter.api.Assertions.*;

public class UserResponseTest {

    @Test
    void testUserResponseFields() {
        UserResponse response = UserResponse.builder().username("dave").email("dave@example.com").fullName("Dave Smith")
                .build();

        assertEquals("dave", response.getUsername());
        assertEquals("dave@example.com", response.getEmail());
        assertEquals("Dave Smith", response.getFullName());
    }
}
