CREATE TABLE password_reset_tokens (
  token VARCHAR(255) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  CONSTRAINT fk_user_password_reset FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
