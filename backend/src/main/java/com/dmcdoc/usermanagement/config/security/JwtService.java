package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.model.User;

import io.jsonwebtoken.Claims;

import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    List<String> extractRoles(String token);

    UUID extractTenantId(String token);

    Claims extractAllClaims(String token);

    Authentication getAuthentication(String token);

    boolean isSuperAdmin(String token);

    boolean isValid(String token);

    String extractToken(HttpServletRequest request);
}
