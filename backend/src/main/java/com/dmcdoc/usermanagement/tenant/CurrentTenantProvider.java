package com.dmcdoc.usermanagement.tenant;

import java.util.UUID;

public interface CurrentTenantProvider {
    UUID getCurrentTenant();
}
