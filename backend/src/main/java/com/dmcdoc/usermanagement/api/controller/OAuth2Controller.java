package com.dmcdoc.usermanagement.api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.dmcdoc.usermanagement.core.service.auth.OAuth2Service;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/auth/oauth2")
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    public OAuth2Controller(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }

    // This endpoint is called after successful OAuth2 login by Spring Security
    @GetMapping("/google")
    public ResponseEntity<?> google(@AuthenticationPrincipal OAuth2User principal) {
        var token = oauth2Service.processOAuth2UserAndGetJwt(principal);
        return ResponseEntity.ok(new JwtDto(token));
    }

    @GetMapping("/facebook")
    public ResponseEntity<?> facebook(@AuthenticationPrincipal OAuth2User principal) {
        var token = oauth2Service.processOAuth2UserAndGetJwt(principal);
        return ResponseEntity.ok(new JwtDto(token));
    }

    public static class JwtDto {
        private String token;

        public JwtDto(String t) {
            this.token = t;
        }

        public String getToken() {
            return token;
        }
    }
}
