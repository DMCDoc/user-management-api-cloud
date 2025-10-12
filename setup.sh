#!/bin/bash
set -e

echo "[SETUP] Creating .env file..."
if [ ! -f .env ]; then
cat > .env <<EOF
POSTGRES_USER=authuser
POSTGRES_PASSWORD=authpass
POSTGRES_DB=authdb
POSTGRES_HOST=postgres
JWT_SECRET=$(openssl rand -hex 32)
JWT_EXPIRATION=3600000
MAIL_HOST=maildev
MAIL_PORT=1025
SPRING_PROFILES_ACTIVE=dev
EOF
  echo "[SETUP] .env created."
else
  echo "[SETUP] .env already exists."
fi

echo "[SETUP] Building project..."
mvn -B -DskipTests clean package

echo "[SETUP] Starting Docker stack..."
docker-compose up --build -d
