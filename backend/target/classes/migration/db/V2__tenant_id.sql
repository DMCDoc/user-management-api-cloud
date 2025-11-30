-- add tenant_id to users (UUID)
ALTER TABLE users ADD COLUMN tenant_id BINARY(16);
CREATE INDEX idx_users_tenant ON users(tenant_id);

-- restaurants
ALTER TABLE restaurants ADD COLUMN tenant_id BINARY(16);
CREATE INDEX idx_restaurants_tenant ON restaurants(tenant_id);
