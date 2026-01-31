package com.dmcdoc.usermanagement.core.service;

/*
Voici les règles SaaS propres, que ton code DOIT respecter :

🔒 Rôles système (SystemTenant.SYSTEM_TENANT)

créés à l’initialisation

jamais modifiables

jamais supprimables

lus uniquement via l’executor

🧑‍💼 Rôles tenant

tenant_id = tenant courant

CRUD autorisé

filtrés par Hibernate
*/

import com.dmcdoc.usermanagement.core.model.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleService {

    Role create(Role role);

    Role update(Role role);

    void delete(UUID id);

    Optional<Role> findById(UUID id);

    Optional<Role> findSystemRole(String roleName);
}
