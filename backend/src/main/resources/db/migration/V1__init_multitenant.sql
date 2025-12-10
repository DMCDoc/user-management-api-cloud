CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===========================
-- TENANT
-- ===========================
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    tenant_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    metadata TEXT
);

-- ===========================
-- ROLES
-- ===========================
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ===========================
-- USERS (tenant-aware)
-- ===========================
CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    password VARCHAR(255),
    provider VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    CONSTRAINT uq_user_email_tenant UNIQUE (email, tenant_id),
    CONSTRAINT uq_user_username_tenant UNIQUE (username, tenant_id)
);

CREATE INDEX idx_user_tenant ON users(tenant_id);

-- ===========================
-- USER → ROLES many-to-many
-- ===========================
CREATE TABLE users_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY(user_id, role_id)
);

-- ===========================
-- RESTAURANTS
-- ===========================
CREATE TABLE restaurants (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    restaurant_name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    metadata TEXT,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_restaurant_tenant ON restaurants(tenant_id);

-- ===========================
-- ADMIN LOGS
-- ===========================
CREATE TABLE admin_logs (
    id UUID PRIMARY KEY,
    admin_email VARCHAR(255),
    action TEXT,
    user_id UUID,
    timestamp TIMESTAMP NOT NULL
);

-- ===========================
-- MAGIC LINK TOKENS
-- ===========================
CREATE TABLE magic_link_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_magic_expiry ON magic_link_tokens(expires_at);
