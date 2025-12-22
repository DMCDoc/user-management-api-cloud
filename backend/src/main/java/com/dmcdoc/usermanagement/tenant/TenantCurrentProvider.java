package com.dmcdoc.usermanagement.tenant;

import java.util.UUID;

public interface TenantCurrentProvider {

    UUID getTenantId();
}
