package com.example.usermanagement.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public class ErrorResponseFactory {

    public static ErrorResponse create(HttpStatus status, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
