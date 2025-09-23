// UserAlreadyExistsException.java
package com.example.sharedcommon.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
