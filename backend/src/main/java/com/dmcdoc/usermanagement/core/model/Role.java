package com.dmcdoc.usermanagement.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name")
})
public class Role {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 80)
    private String name; // e.g. ROLE_SUPER_ADMIN, ROLE_TENANT_ADMIN, ROLE_STAFF, ROLE_USER

    @Column(name = "description")
    private String description;
}
