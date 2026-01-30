package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.sharedcommon.dto.*;
import com.dmcdoc.usermanagement.core.model.*;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    /* OAuth2 */
    User findOrCreateByEmailOAuth2(
            String email,
            OAuth2Provider provider,
            UUID tenantId);

    /* Admin */
    User createAdminForTenant(
            UUID tenantId,
            String email,
            String encodedPassword,
            String firstName,
            String lastName);

    /* Profile */
    Optional<UserResponse> getUserProfile(String username, UUID tenantId);

    /* Deletion */
    void deleteAccountById(UUID userId, UUID tenantId);

    void deleteAccount(String username, UUID tenantId);

    /* Tokens */
    AuthResponse refreshToken(RefreshRequest request, UUID tenantId);

    /* Utilities */
    Optional<User> findByEmailOptional(String email, UUID tenantId);

    void updateProfile(String username, UUID tenantId, RegisterRequest request);
}
