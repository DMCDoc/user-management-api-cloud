package com.dmcdoc.usermanagement.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJwtBuilder {

    private final String secret;
    private final Map<String, Object> claims = new HashMap<>();
    private String subject = "test-user";

    public TestJwtBuilder(String secret) {
        this.secret = secret;
    }

    public TestJwtBuilder withRole(String role) {
        this.claims.put("roles", List.of(role));
        return this;
    }

    public TestJwtBuilder withoutTenant() {
        this.claims.remove("tenant");
        return this;
    }

    public TestJwtBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String build() {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}