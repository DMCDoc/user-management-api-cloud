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

        boolean existsByEmailAndTenantId(String email, UUID tenantId);

        Page<User> findByTenantIdAndUsernameContainingIgnoreCaseOrTenantIdAndEmailContainingIgnoreCase(
                        UUID tenantId,
                        String username,
                        UUID tenantId2,
                        String email,
                        Pageable pageable);

        long countByTenantIdAndRoles_Name(UUID tenantId, String roleName);

        long countByTenantIdAndActiveFalse(UUID tenantId);
}
