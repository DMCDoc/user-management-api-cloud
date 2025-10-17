package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.usermanagement.core.service.MagicLinkService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth/magic")
@RequiredArgsConstructor
@Validated
public class MagicLinkController {

    private final MagicLinkService magicLinkService;

    @PostMapping("/request")
    public ResponseEntity<Void> request(@RequestBody @Validated RequestDto dto) {
        magicLinkService.createAndSendMagicLink(dto.getEmail());
        return ResponseEntity.accepted().build();
    }

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
