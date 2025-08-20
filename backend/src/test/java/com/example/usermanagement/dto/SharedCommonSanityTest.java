package com.example.usermanagement.dto;



import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedCommonSanityTest {

    @Test
    void testUserDto() {
        UserResponse user = new UserResponse(null, null, null);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setFullName("Alice Wonderland");

        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("Alice Wonderland", user.getFullName());
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
