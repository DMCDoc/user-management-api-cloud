// UserAlreadyExistsException.java
package com.dmcdoc.sharedcommon.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
