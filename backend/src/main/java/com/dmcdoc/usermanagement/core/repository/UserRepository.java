package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

        /* ========= GLOBAL (auth, reset, system) ========= */

        Optional<User> findByUsername(String username);

        Optional<User> findByEmail(String email);

        /* ========= TENANT AWARE ========= */

        Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

        Optional<User> findByUsernameAndTenantId(String username, UUID tenantId);

        Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

        boolean existsByIdAndTenantId(UUID id, UUID tenantId);

        void deleteByIdAndTenantId(UUID id, UUID tenantId);

        /* ========= ADMIN / SEARCH ========= */

        Page<User> findByTenantIdAndUsernameContainingIgnoreCaseOrTenantIdAndEmailContainingIgnoreCase(
                        UUID tenantId1,
                        String username,
                        UUID tenantId2,
                        String email,
                        Pageable pageable);

        long countByTenantIdAndRoles_Name(UUID tenantId, String roleName);

        long countByTenantIdAndActiveFalse(UUID tenantId);
}
