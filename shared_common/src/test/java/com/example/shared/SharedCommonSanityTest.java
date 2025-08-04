package com.example.shared;


import com.example.shared.dto.UserDto;
import com.example.shared.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedCommonSanityTest {

    @Test
    void testUserDto() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        assertEquals(1L, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
    }

    @Test
    void testLoginRequest() {
        LoginRequest login = new LoginRequest();
        login.setUsername("bob");
        login.setPassword("secure123");

        assertEquals("bob", login.getUsername());
        assertEquals("secure123", login.getPassword());
    }

    
}
