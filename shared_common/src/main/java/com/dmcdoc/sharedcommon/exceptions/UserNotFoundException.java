// UserNotFoundException.java
package com.dmcdoc.sharedcommon.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}