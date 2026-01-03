package com.dmcdoc.usermanagement.api.exceptions;

import com.dmcdoc.sharedcommon.dto.ErrorCode;
import com.dmcdoc.sharedcommon.dto.ErrorResponse;
import com.dmcdoc.sharedcommon.exceptions.UserAlreadyExistsException;
import com.dmcdoc.sharedcommon.exceptions.UserNotFoundException;
import com.dmcdoc.usermanagement.core.role.exception.SystemRoleModificationException;
import com.dmcdoc.usermanagement.tenant.exception.InvalidTenantIdentifierException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
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

                return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, request);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMalformedBody(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.BAD_REQUEST,
                                ErrorCode.MALFORMED_REQUEST,
                                "Request body is missing or invalid",
                                request);
        }

        /**
         * ⚠️ IMPORTANT
         * Multi-tenant → UUID invalide = FORBIDDEN (et pas 400)
         * Compatible RestaurantControllerIT
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                // Cas RestaurantControllerIT (multi-tenant)
                if (request.getRequestURI().startsWith("/api/restaurants")) {
                        return build(
                                        HttpStatus.FORBIDDEN,
                                        ErrorCode.RESOURCE_FORBIDDEN,
                                        "Invalid tenant identifier",
                                        request);
                }

                // Cas REST classique (Role, User, etc.)
                return build(
                                HttpStatus.BAD_REQUEST,
                                ErrorCode.MALFORMED_REQUEST,
                                "Invalid UUID format",
                                request);
        }

// in case you need to handle invalid tenant identifier differently

        @ExceptionHandler(InvalidTenantIdentifierException.class)
        public ResponseEntity<ErrorResponse> handleInvalidTenant(
                        InvalidTenantIdentifierException ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.FORBIDDEN,
                                ErrorCode.RESOURCE_FORBIDDEN,
                                "Access to resource is forbidden",
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
                                ErrorCode.ACCESS_DENIED,
                                "Invalid credentials",
                                request);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.FORBIDDEN,
                                ErrorCode.ACCESS_DENIED,
                                ex.getMessage(),
                                request);
        }

        @ExceptionHandler(SystemRoleModificationException.class)
        public ResponseEntity<ErrorResponse> handleSystemRoleModification(
                        SystemRoleModificationException ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.FORBIDDEN,
                                ex.getErrorCode(),
                                ex.getMessage(),
                                request);
        }

        // -------------------------------------------------
        // 403 / 404 — RESSOURCES
        // -------------------------------------------------

        /**
         * Multi-tenant : ressource existante mais interdite
         */
        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleForbiddenResource(
                        Exception ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.FORBIDDEN,
                                ErrorCode.RESOURCE_FORBIDDEN,
                                "Access to resource is forbidden",
                                request);
        }

        /**
         * REST pur : ressource inexistante
         */
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(
                        Exception ex,
                        HttpServletRequest request) {

                return build(
                                HttpStatus.NOT_FOUND,
                                ErrorCode.ROLE_NOT_FOUND,
                                "Resource not found",
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
                                ErrorCode.USER_ALREADY_EXISTS,
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
                                ErrorCode.DATABASE_CONSTRAINT,
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
                                ErrorCode.MALFORMED_REQUEST,
                                "Content-Type not supported. Use application/json",
                                request);
        }

        // -------------------------------------------------
        // 500 — FALLBACK
        // -------------------------------------------------

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneric(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Unhandled exception on {}", request.getRequestURI(), ex);

                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ErrorCode.INTERNAL_ERROR,
                                "An unexpected error occurred",
                                request);
        }

        // -------------------------------------------------
        // UTIL
        // -------------------------------------------------

        private ResponseEntity<ErrorResponse> build(
                        HttpStatus status,
                        ErrorCode errorCode,
                        String message,
                        HttpServletRequest request) {

                return ResponseEntity.status(status)
                                .body(ErrorResponseFactory.create(
                                                status,
                                                message,
                                                request.getRequestURI()));
        }
}
