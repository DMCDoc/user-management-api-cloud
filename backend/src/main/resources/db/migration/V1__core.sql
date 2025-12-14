/* table commune */
 CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY,
    tenant_key VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
