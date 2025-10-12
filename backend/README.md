+# Backend README — user-management-api-cloud (backend)
+
+## Overview
+
+This README documents the backend module of the `user-management-api-cloud` project.
+It focuses on running the backend locally (Docker + Docker Compose), running integration
+tests using Testcontainers (Postgres), and switching to production databases.
+
+The backend is a Spring Boot application. This document assumes you run commands from
+the repository root and use the Maven wrapper `./mvnw`.
+
+---
+
+## Prerequisites
+
+- Java 17
+- Docker & Docker Compose
+- Git
+- (Optional) Maven if not using the wrapper
+
+---
+
+## Environment files
+
+The patch includes a `.env` at the repository root. It contains the variables used
+by Docker Compose and by the `application-dev.properties`.
+
+Default `.env` values (do not use these in production):
+
+```
+POSTGRES_DB=usermgmt
+POSTGRES_USER=usermgmt
+POSTGRES_PASSWORD=usermgmt_pass
+POSTGRES_PORT=5432
+SPRING_PROFILES_ACTIVE=dev
+JWT_SECRET=change-me-in-prod
+```
+
+Place your production secrets in a secure store (Vault, K8s secrets, systemd environment)
+— do **not** commit production secrets to the repository.
+
+---
+
+## Docker Compose (local DB)
+
+To spin up a local PostgreSQL for development:
+
+```bash
+docker compose -f backend/docker-compose.yml up --build -d
+```
+
+This will start a Postgres 15 container with database `usermgmt` and user `usermgmt`.
+
+To tear down:
+
+```bash
+docker compose -f backend/docker-compose.yml down -v
+```
+
+---
+
+## Running the backend locally (dev profile)
+
+1. Ensure `.env` values are set (or export env vars). The `application-dev.properties` uses
+   the environment variables to configure the datasource.
+
+2. Build and run the backend with the Maven wrapper:
+
+```bash
+./mvnw -pl backend -am clean package
+java -jar backend/target/<your-backend-jar>.jar --spring.profiles.active=dev
+```
+
+Or run directly with the wrapper in dev mode (tests excluded):
+
+```bash
+./mvnw -pl backend -am -DskipTests spring-boot:run -Dspring-boot.run.profiles=dev
+```
+
+---
+
+## Integration tests (Testcontainers + PostgreSQL)
+
+The repository includes an integration test using Testcontainers that starts an ephemeral
+PostgreSQL container and runs a `DataJpaTest` to validate repository behavior.
+
+Run tests with the Maven wrapper:
+
+```bash
+./mvnw -pl backend clean test
+```
+
+This requires Docker to be running on the machine so Testcontainers can start the
+PostgreSQL container.
+
+---
+
+## Switching from Docker-local DB to a production DB
+
+For production, we recommend:
+
+1. Set `SPRING_PROFILES_ACTIVE=prod` and create `application-prod.properties` with production
+   JDBC URL, username, and password.
+2. Use a secret management system (Vault, cloud KMS, or environment injection via systemd)
+   instead of embedding secrets in files.
+3. Ensure your JWT secret (`JWT_SECRET`) is stored securely and rotated regularly.
+
+Example systemd unit snippet to pass secrets as environment variables:
+
+```ini
+[Service]
+Environment=SPRING_PROFILES_ACTIVE=prod
+Environment=DB_URL=jdbc:postgresql://db.example.com:5432/usermgmt
+Environment=DB_USER=prod_user
+Environment=DB_PASS=prod_pass
+Environment=JWT_SECRET=@vault/path#jwt_secret
+```
+
+---
+
+## Notes & Next steps
+
+- This README focuses on the backend only.
+- Once the backend is stable and tested, a top-level README will describe the full stack
+  (frontend + backend + infra).
+
+
+