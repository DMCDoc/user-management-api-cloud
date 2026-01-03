package com.dmcdoc.usermanagement.api.exceptions;

import com.dmcdoc.sharedcommon.dto.ErrorResponse;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    public static ErrorResponse create(
            HttpStatus status,
            String message,
            String path) {

        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path);
    }
}
