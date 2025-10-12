package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.sharedcommon.dto.ErrorResponse;
import com.dmcdoc.usermanagement.api.exceptions.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public CustomAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse body = ErrorResponseFactory.create(HttpStatus.FORBIDDEN, "Access denied",
                request.getRequestURI());

        mapper.writeValue(response.getOutputStream(), body);
    }
}

// 403 Forbidden