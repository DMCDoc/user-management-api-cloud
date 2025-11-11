package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.usermanagement.core.service.auth.MagicLinkService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/auth/magic")
@RequiredArgsConstructor
@Validated
public class MagicLinkController {

    private final MagicLinkService magicLinkService;

    @Operation(summary = "Demande un lien magique par email", description = "Envoie un lien d'authentification à usage unique.")
    @PostMapping("/request")
    public ResponseEntity<Void> request(@RequestBody @Validated RequestDto dto) {
        magicLinkService.createAndSendMagicLink(dto.getEmail());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Vérifie un lien magique", description = "Valide le token reçu par mail et renvoie un JWT.")
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestParam("token") String token) {
        AuthResponse resp = magicLinkService.verifyAndAuthenticate(token);
        return ResponseEntity.ok(resp);
    }

    @Data
    public static class RequestDto {
        @Email
        @NotBlank
        private String email;
    }

    
}
