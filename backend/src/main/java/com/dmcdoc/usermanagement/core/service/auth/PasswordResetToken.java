package com.dmcdoc.usermanagement.core.service.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.dmcdoc.usermanagement.core.model.User user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    public PasswordResetToken() {
        this.token = UUID.randomUUID().toString();
    }

    // getters / setters

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }

    // token getter for convenience
    public String getToken() {
        return token;
    }

    public void setUser(com.dmcdoc.usermanagement.core.model.User user) {
        this.user = user;
    }

    public com.dmcdoc.usermanagement.core.model.User getUser() {
        return user;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
    

}
