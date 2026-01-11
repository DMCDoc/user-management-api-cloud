package com.dmcdoc.usermanagement.dto;


import org.junit.jupiter.api.Test;

import com.dmcdoc.sharedcommon.dto.UserResponse;

import static org.junit.jupiter.api.Assertions.*;

public class UserDtoTest {

@Test
void testUserDtoFields() {
    UserResponse user = UserResponse.builder()
            .username("john_doe")
            .email("john@example.com")
            .fullName("John Doe")
            .build();

    assertEquals("john_doe", user.getUsername());
    assertEquals("john@example.com", user.getEmail());
    assertEquals("John Doe", user.getFullName());
}
}
