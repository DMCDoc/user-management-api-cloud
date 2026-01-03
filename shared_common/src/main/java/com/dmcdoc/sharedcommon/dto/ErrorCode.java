package com.dmcdoc.sharedcommon.dto;

public enum ErrorCode {

    // --- Sécurité / multi-tenant
    ACCESS_DENIED,
    INVALID_UUID,
    RESOURCE_FORBIDDEN,

    // --- Rôles
    SYSTEM_ROLE_IMMUTABLE,
    ROLE_NOT_FOUND,

    // --- Validation / format
    VALIDATION_ERROR,
    MALFORMED_REQUEST,

    // --- Conflits
    USER_ALREADY_EXISTS,
    DATABASE_CONSTRAINT,

    // --- Fallback
    INTERNAL_ERROR
}
