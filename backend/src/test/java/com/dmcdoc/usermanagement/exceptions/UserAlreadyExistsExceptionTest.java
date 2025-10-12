package com.dmcdoc.usermanagement.exceptions;

import org.junit.jupiter.api.Test;

import com.dmcdoc.sharedcommon.exceptions.UserAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.*;

public class UserAlreadyExistsExceptionTest {

    @Test
    void testMessage() {
        String message = "L'utilisateur existe déjà";
        UserAlreadyExistsException ex = new UserAlreadyExistsException(message);
        assertEquals(message, ex.getMessage());
    }

    @Test
    void testInheritance() {
        assertTrue(new UserAlreadyExistsException("Test") instanceof RuntimeException);
    }
}
