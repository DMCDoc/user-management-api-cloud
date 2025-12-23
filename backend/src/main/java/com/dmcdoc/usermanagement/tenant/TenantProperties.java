package com.dmcdoc.usermanagement.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.tenant")
public class TenantProperties {

        private TenantMode mode = TenantMode.HEADER;
        private boolean allowHeader = true;
        private boolean allowSubdomain = false;

        private String header = "X-Tenant-ID";
        private String defaultTenant = "tenant-test";

        public TenantMode getMode() {
                return mode;
        }

        public void setMode(TenantMode mode) {
                this.mode = mode;
        }

        public boolean isAllowHeader() {
                return allowHeader;
        }

        public void setAllowHeader(boolean allowHeader) {
                this.allowHeader = allowHeader;
        }

        public boolean isAllowSubdomain() {
                return allowSubdomain;
        }

        public void setAllowSubdomain(boolean allowSubdomain) {
                this.allowSubdomain = allowSubdomain;
        }

        public String getHeader() {
                return header;
        }

        public void setHeader(String header) {
                this.header = header;
        }

        public String getDefaultTenant() {
                return defaultTenant;
        }

        public void setDefaultTenant(String defaultTenant) {
                this.defaultTenant = defaultTenant;
        }
}
