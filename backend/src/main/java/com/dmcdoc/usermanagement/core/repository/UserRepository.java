package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository
        extends TenantAwareRepository<User, UUID> {

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    Optional<User> findByUsernameAndTenantId(String username, UUID tenantId);

    Optional<User> findByEmail(String email);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);

    long countByRoles_Name(String roleName);

    long countByActiveFalse();
}
