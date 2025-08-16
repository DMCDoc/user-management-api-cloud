package com.example.usermanagement.exceptions;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponse(String message, int status, String path) {
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
