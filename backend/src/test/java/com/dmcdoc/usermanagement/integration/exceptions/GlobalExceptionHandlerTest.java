package com.dmcdoc.usermanagement.integration.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dmcdoc.usermanagement.api.controller.DummyController;
import com.dmcdoc.usermanagement.api.exceptions.GlobalExceptionHandler;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private DummyController dummyController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dummyController).setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @ParameterizedTest @MethodSource("exceptionScenarios")
    void testExceptionHandling(String url, int expectedStatus, String expectedError, String expectedMessage)
            throws Exception {
        mockMvc.perform(get(url)).andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.error").value(expectedError))
                .andExpect(jsonPath("$.message").value(expectedMessage)).andExpect(jsonPath("$.path").value(url));
    }

    private static Stream<Arguments> exceptionScenarios() {
        return Stream.of(Arguments.of("/api/dummy/fail/user-exists", 409, "Conflict", "Test: User already exists"),
                Arguments.of("/api/dummy/fail/user-not-found", 404, "Not Found", "Test: User not found"),
                Arguments.of("/api/dummy/illegal-state", 409, "Conflict", "Conflict error"),
                Arguments.of("/api/dummy/unhandled", 500, "Internal Server Error", "An unexpected error occurred"),
                Arguments.of("/api/dummy/illegal-arg", 400, "Bad Request", "Invalid input"));
    }
}
