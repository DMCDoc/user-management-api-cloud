package com.dmcdoc.usermanagement.core.role.exception;

import com.dmcdoc.sharedcommon.dto.ErrorCode;
import lombok.Getter;

@Getter
public class SystemRoleModificationException extends RuntimeException {

    private final ErrorCode errorCode;

    public SystemRoleModificationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
