package com.dmcdoc.usermanagement.core.service.auth;

import java.util.Optional;

import com.dmcdoc.usermanagement.core.model.RefreshToken;
import com.dmcdoc.usermanagement.core.model.User;

public interface RefreshTokenService {

    RefreshToken create(User user);

    Optional<RefreshToken> findValid(String token);

    void revokeAll(User user);
}
