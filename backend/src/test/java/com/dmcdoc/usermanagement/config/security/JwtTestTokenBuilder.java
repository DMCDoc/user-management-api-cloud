package com.dmcdoc.usermanagement.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class JwtTestTokenBuilder {

    private UUID tenantId;
    private String role = "ROLE_USER";
    private boolean expired = false;
    private boolean withoutTenant = false;

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public JwtTestTokenBuilder withTenant(UUID tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public JwtTestTokenBuilder withoutTenant() {
        this.withoutTenant = true;
        return this;
    }

    public JwtTestTokenBuilder withRole(String role) {
        this.role = role;
        return this;
    }

    public JwtTestTokenBuilder expired() {
        this.expired = true;
        return this;
    }

    public String build() {
        Instant now = Instant.now();
        Instant expiration = expired
                ? now.minusSeconds(60)
                : now.plusSeconds(3600);

        var builder = Jwts.builder()
                .setSubject("test-user")
                .claim("roles", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256);

        if (!withoutTenant && tenantId != null) {
            builder.claim("tenantId", tenantId.toString());
        }

        return builder.compact();
    }
}
