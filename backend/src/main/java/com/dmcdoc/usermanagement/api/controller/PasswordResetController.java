package com.dmcdoc.usermanagement.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dmcdoc.usermanagement.core.service.auth.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        service.createPasswordResetToken(email);
        return ResponseEntity.ok(Map.of("message", "Si l'email existe, un lien a été envoyé."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");
        try {
            service.resetPassword(token, password);
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
