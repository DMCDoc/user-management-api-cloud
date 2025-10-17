CREATE TABLE IF NOT EXISTS magic_link_tokens (
  id VARCHAR(36) PRIMARY KEY,
  token VARCHAR(128) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  used boolean NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_magic_token ON magic_link_tokens(token);
