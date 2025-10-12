package com.dmcdoc.usermanagement.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.dmcdoc.usermanagement.api.exceptions.GlobalExceptionHandler;
import com.example.sharedcommon.dto.ErrorResponse;
import com.example.sharedcommon.exceptions.UserAlreadyExistsException;
import com.example.sharedcommon.exceptions.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerUnitTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
    }

    @Test
    void testHandleIllegalArgumentException_withPath() {
        when(mockRequest.getRequestURI()).thenReturn("/api/test");

        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        ResponseEntity<ErrorResponse> result = handler.handleIllegalArgument(ex, mockRequest);

        assertEquals(400, result.getStatusCode().value());
        ErrorResponse response = result.getBody();
        assertNotNull(response);
        assertEquals("Invalid input", response.getMessage());
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("/api/test", response.getPath());
    }

    @Test
    void testHandleIllegalStateException_withPath() {
        when(mockRequest.getRequestURI()).thenReturn("/api/conflict");

        IllegalStateException ex = new IllegalStateException("Conflict error");
        ResponseEntity<ErrorResponse> result = handler.handleIllegalState(ex, mockRequest);

        assertEquals(409, result.getStatusCode().value());
        ErrorResponse response = result.getBody();
        assertNotNull(response);
        assertEquals("Conflict error", response.getMessage());
        assertEquals(409, response.getStatus());
        assertEquals("Conflict", response.getError());
        assertEquals("/api/conflict", response.getPath());
    }

    @Test
    void testHandleUserNotFound_withPath() {
        when(mockRequest.getRequestURI()).thenReturn("/api/users/42");

        UserNotFoundException ex = new UserNotFoundException("User not found");
        ResponseEntity<ErrorResponse> result = handler.handleUserNotFound(ex, mockRequest);

        assertEquals(404, result.getStatusCode().value());
        ErrorResponse response = result.getBody();
        assertNotNull(response);
        assertEquals("User not found", response.getMessage());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("/api/users/42", response.getPath());
    }

    @Test
    void testHandleUserAlreadyExists_withPath() {
        when(mockRequest.getRequestURI()).thenReturn("/api/users");

        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");
        ResponseEntity<ErrorResponse> result = handler.handleUserAlreadyExists(ex, mockRequest);

        assertEquals(409, result.getStatusCode().value());
        ErrorResponse response = result.getBody();
        assertNotNull(response);
        assertEquals("User already exists", response.getMessage());
        assertEquals(409, response.getStatus());
        assertEquals("Conflict", response.getError());
        assertEquals("/api/users", response.getPath());
    }

    @Test
    void testHandleGenericException_withPath() {
        when(mockRequest.getRequestURI()).thenReturn("/api/unknown");

        Exception ex = new Exception("Unexpected");
        ResponseEntity<ErrorResponse> result = handler.handleGenericException(ex, mockRequest);

        assertEquals(500, result.getStatusCode().value());
        ErrorResponse response = result.getBody();
        assertNotNull(response);
        assertEquals("An unexpected error occurred", response.getMessage());
        assertEquals(500, response.getStatus());
        assertEquals("Internal Server Error", response.getError());
        assertEquals("/api/unknown", response.getPath());
    }
}
