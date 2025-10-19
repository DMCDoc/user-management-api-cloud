# User Management API Cloud (Spring Boot 3.5.4)

## 🚀 Description
Projet de gestion d’utilisateurs complet avec plusieurs modes d’authentification :
- 🔑 Username / Password (JSON)
- ✉️ Magic Link (token envoyé par email)
- 🌍 OAuth2 (Google / GitHub)
- 🧩 JWT pour les endpoints protégés

---

## 🧱 Stack minimale

| Service | Image | Port local |
|----------|--------|-------------|
| PostgreSQL | postgres:16 | 5432 |
| MailDev | maildev/maildev | 1080 (UI), 1025 (SMTP) |
| Backend | user-management-api-cloud/backend | 8080 |

> RabbitMQ, Memcached et Elasticsearch sont optionnels et peuvent être ajoutés plus tard.

---

## ⚙️ Installation rapide

1. Cloner le dépôt :
   ```bash
   git clone https://github.com/DMCDoc/user-management-api-cloud.git
   cd user-management-api-cloud
