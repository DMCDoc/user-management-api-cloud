package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    /* ========= GLOBAL ========= */

    Optional<Role> findByName(String name);

    /* ========= TENANT AWARE ========= */

    Optional<Role> findByNameAndTenantId(String name, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);

    List<Role> findAllByTenantId(UUID tenantId);
}
