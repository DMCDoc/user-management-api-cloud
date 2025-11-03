# Environnement de développement

## 🏗 Structure
```
/project-root
 ├── backend/                # Spring Boot
 ├── frontend/               # React (Vite ou Create React App)
 ├── docker-compose.dev.yml
 ├── .env.dev
 └── README.dev.md
```

## ⚙️ Prérequis
- Docker & Docker Compose
- Node.js (si build manuel du frontend)
- JDK 21+
- Maven 3.9+

## 🔑 Variables d’environnement (.env.dev)
Exemple minimal :
```
POSTGRES_USER=devuser
POSTGRES_PASSWORD=devpass
POSTGRES_DB=devdb
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=devsecret
RABBITMQ_USER=dev
RABBITMQ_PASS=devpass
```
generation de cle JWT 

openssl rand -base64 32

## 🚀 Lancer l’environnement
Depuis la racine :
```bash
docker compose -f docker-compose.dev.yml up --build -d
docker compose --env-file .env.dev -f docker-compose.dev.yml up -d --build
docker-compose -f docker-compose.dev.yml run --rm frontend npm install


check
docker compose -f docker-compose.dev.yml ps
docker logs -f um_backend_dev
docker exec -it um_backend_dev sh
docker compose -f docker-compose.dev.yml build --no-cache backend
mvn spring-boot:run -X
show_table docker exec um_postgres_dev psql -U authuser -d authdb -c "SELECT username, email, password FROM users;"
# Testez les deux chemins
curl -v http://localhost/auth/login
curl -v http://localhost/api/auth/login
stop
docker-compose -f docker-compose.dev.yml down
java tools option: -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005

```

## 🧠 Services inclus
| Service | Port local | Description |
|----------|-------------|-------------|
| React Frontend | 5173 | Mode développement Vite |
| Spring Boot Backend | 8080 | API |
| PostgreSQL | 5432 | Base de données |
| RabbitMQ | 15672 (UI) / 5672 | Broker de messages |
| Memcached | 11211 | Cache |
| Elasticsearch | 9200 | Moteur de recherche |

## 🔄 Rebuild complet
```bash
docker compose -f docker-compose.dev.yml down -v
docker compose -f docker-compose.dev.yml up --build
```

## 🧪 Tester le backend
```bash
cd backend
mvn test
```

## 🧩 Notes
- Le frontend appelle le backend via `http://localhost:8080`.
- Le hot reload React est actif.
- Logs visibles avec :
  ```bash
  docker compose -f docker-compose.dev.yml logs -f
  ```
