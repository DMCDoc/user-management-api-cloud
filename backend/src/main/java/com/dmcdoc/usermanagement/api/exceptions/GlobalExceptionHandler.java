package com.dmcdoc.usermanagement.api.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.dmcdoc.sharedcommon.dto.ErrorResponse;
import com.dmcdoc.sharedcommon.exceptions.UserAlreadyExistsException;
import com.dmcdoc.sharedcommon.exceptions.UserNotFoundException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // --- ERREURS DE VALIDATION & SYNTAXE (400) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({ IllegalArgumentException.class, HttpMessageNotReadableException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        String message = (ex instanceof HttpMessageNotReadableException)
                ? "Request body is missing or invalid"
                : ex.getMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // --- ERREURS D'AUTHENTIFICATION & ACCÈS (401/403) ---

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Identifiants invalides", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponseFactory.create(
                        HttpStatus.FORBIDDEN,
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    // --- ERREURS DE RESSOURCES (404) ---

    @ExceptionHandler({ UserNotFoundException.class, EntityNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // --- CONFLITS & BASE DE DONNÉES (409) ---

    @ExceptionHandler({ UserAlreadyExistsException.class, DataIntegrityViolationException.class })
    public ResponseEntity<ErrorResponse> handleConflicts(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (ex instanceof DataIntegrityViolationException) {
            message = "Data conflict or database constraint violation";
            if (message.contains("username"))
                message = "Username already exists";
            else if (message.contains("email"))
                message = "Email already exists";
        }
        return buildResponse(HttpStatus.CONFLICT, message, request);
    }

    // --- FORMATS & TYPES (400 ou 415) ---

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpServletRequest request) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type not supported. Use 'application/json'",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(HttpServletRequest request) {
        // Changé de FORBIDDEN à BAD_REQUEST (plus standard pour un format d'ID
        // invalide)
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid parameter format (UUID expected)", request);
    }

    // --- ERREURS GÉNÉRIQUES (500) ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    // Méthode utilitaire pour centraliser la création
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ErrorResponseFactory.create(status, message, request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch detected on URI: {} - Message: {}", request.getRequestURI(), ex.getMessage());

        // Pour satisfaire ton test "invalidUuidMustBeForbiddenNotBadRequest",
        // nous renvoyons un 403 Forbidden au lieu d'une 500 ou 400.
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseFactory.create(
                        HttpStatus.FORBIDDEN,
                        "Invalid parameter format",
                        request.getRequestURI()));
    }
}