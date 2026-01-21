
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = UUID.class)
)
package com.dmcdoc.usermanagement.core.model;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;



import java.util.UUID;