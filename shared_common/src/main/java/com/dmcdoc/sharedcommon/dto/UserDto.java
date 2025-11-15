package com.dmcdoc.sharedcommon.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public class UserDto {
    private UUID id;
    private String email;
    private Instant createdAt;
    private boolean blocked;
    private List<String> roles;

    public UserDto(UUID id, String email, Instant createdAt, boolean blocked, List<String> roles) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt;
        this.blocked = blocked;
        this.roles = roles;
    }

    public static UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getCreatedAt(),
                u.isBlocked(),
                u.getRoles().stream().map(r -> r.getName()).toList());
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public List<String> getRoles() {
        return roles;

    }

    class User {
        private java.util.UUID id;
        private String email;
        private java.time.Instant createdAt;
        private boolean blocked;
        private java.util.List<Role> roles;

        public java.util.UUID getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public java.time.Instant getCreatedAt() {
            return createdAt;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public java.util.List<Role> getRoles() {
            return roles;
        }
    }

    class Role {
        private String name;

        public String getName() {
            return name;
        }
    }
}
