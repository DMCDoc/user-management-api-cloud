package com.example.shared.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {

    @Test
    void testValidEmails() {
        assertTrue(EmailValidator.isValid("test@example.com"));
        assertTrue(EmailValidator.isValid("user.name+tag@domain.co.uk"));
    }

    @Test
    void testInvalidEmails() {
        assertFalse(EmailValidator.isValid("invalid@"));
        assertFalse(EmailValidator.isValid("no-at-symbol.com"));
        assertFalse(EmailValidator.isValid(""));
        assertFalse(EmailValidator.isValid(null));
    }
}
// This test class checks the functionality of the EmailValidator class.
// It includes tests for valid email formats and various invalid cases, ensuring that the validator behaves as