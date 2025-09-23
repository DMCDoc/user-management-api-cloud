// UserNotFoundException.java
package com.example.sharedcommon.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}