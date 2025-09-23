package com.example.usermanagement.api.exceptions;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

import com.example.sharedcommon.dto.ErrorResponse;

public class ErrorResponseFactory {

    public static ErrorResponse create(HttpStatus status, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
