package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.api.exceptions.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        var errorResponse = ErrorResponseFactory.create(
                HttpStatus.FORBIDDEN,
                accessDeniedException.getMessage(),
                request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
