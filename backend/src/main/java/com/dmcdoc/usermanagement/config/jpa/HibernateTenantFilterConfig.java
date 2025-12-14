/*
Définir le Filter Hibernate (OBLIGATOIRE) pour le multi-tenancy basé sur les entités.
*/

package com.dmcdoc.usermanagement.config.jpa;

import java.util.UUID;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.context.annotation.Configuration;

@Configuration
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
public class HibernateTenantFilterConfig {
}
