package com.dmcdoc.usermanagement.exceptions;

import org.junit.jupiter.api.Test;

import com.example.sharedcommon.exceptions.EmailAlreadyUsedException;

import static org.junit.jupiter.api.Assertions.*;

public class EmailAlreadyUsedExceptionTest {

    @Test
    void testMessage() {
        String message = "Email déjà utilisé";
        EmailAlreadyUsedException ex = new EmailAlreadyUsedException(message);
        assertEquals(message, ex.getMessage());
    }

    @Test
    void testInheritance() {
        assertTrue(new EmailAlreadyUsedException("Test") instanceof RuntimeException);
    }
}
