package com.dmcdoc.usermanagement.api.exceptions;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.dmcdoc.sharedcommon.dto.ErrorResponse;
import com.dmcdoc.sharedcommon.exceptions.UserAlreadyExistsException;
import com.dmcdoc.sharedcommon.exceptions.UserNotFoundException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
                    HttpServletRequest request) {
            String message = ex.getBindingResult().getFieldErrors().stream()
                            .map(error -> error.getField() + " " + error.getDefaultMessage())
                            .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest()
                            .body(ErrorResponseFactory.create(HttpStatus.BAD_REQUEST, message,
                                            request.getRequestURI()));
    }
    
    

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
            HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(ErrorResponseFactory.create(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseFactory.create(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseFactory.create(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex,
                    HttpServletRequest request) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                            ErrorResponseFactory.create(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()));
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Identifiants invalides"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("error", ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
            // ✅ LOGGEZ l'exception réelle
            log.error("Unhandled exception in request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponseFactory
                            .create(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred",
                                            request.getRequestURI()));
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                HttpServletRequest request) {
        String message = "Request body is missing or invalid";
        if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
                message = "Request body is required for this endpoint";
        }
        return ResponseEntity.badRequest()
                        .body(ErrorResponseFactory.create(HttpStatus.BAD_REQUEST, message, request.getRequestURI()));
}

@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                HttpServletRequest request) {
        String message = "Content-Type not supported. Please use 'application/json'";
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(ErrorResponseFactory.create(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message,
                                        request.getRequestURI()));
}

@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
        HttpServletRequest request) {
    String message = "Database error occurred";
    
    if (ex.getMessage().contains("duplicate key") || ex.getMessage().contains("already exists")) {
        if (ex.getMessage().contains("username")) {
            message = "Username already exists";
        } else if (ex.getMessage().contains("email")) {
            message = "Email already exists";
        } else {
            message = "Data already exists";
        }
    }
    
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponseFactory.create(HttpStatus.CONFLICT, message, request.getRequestURI()));
}

}
