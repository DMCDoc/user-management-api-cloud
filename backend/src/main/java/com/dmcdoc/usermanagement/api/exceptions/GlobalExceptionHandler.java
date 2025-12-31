package com.dmcdoc.usermanagement.api.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.dmcdoc.sharedcommon.dto.ErrorResponse;
import com.dmcdoc.sharedcommon.exceptions.UserAlreadyExistsException;
import com.dmcdoc.sharedcommon.exceptions.UserNotFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // -------------------------------------------------
        // 400 — VALIDATION / FORMAT
        // -------------------------------------------------

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(err -> err.getField() + " " + err.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                return build(HttpStatus.BAD_REQUEST, message, request);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMalformedBody(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.BAD_REQUEST,
                                "Request body is missing or invalid",
                                request);
        }

        // -------------------------------------------------
        // 🔑 MULTI-TENANT — UUID invalide ⇒ 403
        // -------------------------------------------------

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleUuidMismatch(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                log.warn("Invalid path variable: {}", ex.getMessage());

                return build(
                                HttpStatus.FORBIDDEN,
                                "Invalid UUID",
                                request);
        }

        // -------------------------------------------------
        // 401 / 403 — SÉCURITÉ
        // -------------------------------------------------

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                        BadCredentialsException ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid credentials",
                                request);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.FORBIDDEN,
                                "Access is denied",
                                request);
        }

        // -------------------------------------------------
        // 🔑 MULTI-TENANT — RESSOURCE INTERDITE
        // -------------------------------------------------

        @ExceptionHandler({
                        UserNotFoundException.class,
                        EntityNotFoundException.class
        })
        public ResponseEntity<ErrorResponse> handleForbiddenResource(
                        Exception ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.FORBIDDEN,
                                "Access to resource is forbidden",
                                request);
        }

        // -------------------------------------------------
        // 409 — CONFLITS
        // -------------------------------------------------

        @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
                        UserAlreadyExistsException ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.CONFLICT,
                                ex.getMessage(),
                                request);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDatabaseConflict(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request) {

                log.warn("Database constraint violation", ex);

                return build(
                                HttpStatus.CONFLICT,
                                "Database constraint violation",
                                request);
        }

        // -------------------------------------------------
        // 415 — MEDIA TYPE
        // -------------------------------------------------

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleUnsupportedMedia(
                        HttpServletRequest request) {

                return build(
                                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                "Content-Type not supported. Use application/json",
                                request);
        }

        // -------------------------------------------------
        // 500 — FALLBACK (JAMAIS en test)
        // -------------------------------------------------

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneric(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Unhandled exception on {}", request.getRequestURI(), ex);

                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected error occurred",
                                request);
        }

        // -------------------------------------------------
        // UTIL
        // -------------------------------------------------

        private ResponseEntity<ErrorResponse> build(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request) {

                return ResponseEntity.status(status)
                                .body(ErrorResponseFactory.create(
                                                status,
                                                message,
                                                request.getRequestURI()));
        }
}
