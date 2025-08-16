package com.example.usermanagement.controller;

import com.example.usermanagement.exceptions.UserAlreadyExistsException;
import com.example.usermanagement.exceptions.UserNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/dummy")
public class DummyController {

    @GetMapping("/fail/user-exists")
    public String triggerUserExists() {
        throw new UserAlreadyExistsException("Test: User already exists");
    }

    @GetMapping("/fail/user-not-found")
    public String triggerUserNotFound() {
        throw new UserNotFoundException("Test: User not found");
    }

    @GetMapping("/illegal-arg")
    public String illegalArg() {
        throw new IllegalArgumentException("Invalid input");
    }

    @GetMapping("/illegal-state")
    public String triggerIllegalState() {
        throw new IllegalStateException("Conflict error");
    }

    @GetMapping("/unhandled")
    public String triggerUnhandledException() {
        throw new RuntimeException("Unexpected exception");
    }
}
