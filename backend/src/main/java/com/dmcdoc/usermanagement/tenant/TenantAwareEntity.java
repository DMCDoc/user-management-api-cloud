package com.dmcdoc.usermanagement.tenant;

import java.util.UUID;

public interface TenantAwareEntity {

    UUID getTenantId();
   
}
