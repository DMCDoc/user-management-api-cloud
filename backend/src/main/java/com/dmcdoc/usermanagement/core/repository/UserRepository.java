package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndTenantId(String username, UUID tenantId);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email,
            Pageable pageable);

    long countByRoles_Name(String roleName);

    long countByEnabledFalse();
}
