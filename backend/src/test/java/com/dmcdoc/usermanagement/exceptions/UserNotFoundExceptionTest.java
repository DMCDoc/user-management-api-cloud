package com.dmcdoc.usermanagement.exceptions;

import org.junit.jupiter.api.Test;

import com.dmcdoc.sharedcommon.exceptions.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class UserNotFoundExceptionTest {

    @Test
    void testMessage() {
        String message = "Utilisateur introuvable";
        UserNotFoundException ex = new UserNotFoundException(message);
        assertEquals(message, ex.getMessage());
    }

    @Test
    void testInheritance() {
        assertTrue(new UserNotFoundException("Test") instanceof RuntimeException);
    }
}
