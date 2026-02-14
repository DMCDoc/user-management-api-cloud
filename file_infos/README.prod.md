# Environnement de production

## 🏗 Structure
```
/project-root
 ├── backend/                 # Spring Boot (packagé en JAR)
 ├── frontend/                # React (buildé en fichiers statiques)
 ├── nginx/
 │   └── default.conf         # Reverse proxy Nginx
 ├── docker-compose.prod.yml
 ├── .env.prod
 └── README.prod.md
```

## ⚙️ Prérequis
- Docker & Docker Compose
- Certificat SSL (optionnel)
- Domaine ou IP fixe

## 🔑 Variables d’environnement (.env.prod)
```
POSTGRES_USER=produser
POSTGRES_PASSWORD=strongpass
POSTGRES_DB=proddb
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=verystrongsecret
RABBITMQ_USER=prod
RABBITMQ_PASS=strongpass
```

## 🧱 Construction des images

### 1️⃣ Backend
```bash
cd backend
mvn clean package -DskipTests
```
Le fichier `target/backend-1.0.jar` sera copié dans l’image Docker.

### 2️⃣ Frontend
```bash
cd frontend
npm install
npm run build
```
Le dossier `dist/` (ou `build/`) sera servi par Nginx.

## 🚀 Lancer la stack
Depuis la racine :
```bash
docker compose -f docker-compose.prod.yml up --build -d
```

## 🌐 Accès aux services
| Service | Port | Description |
|----------|------|-------------|
| Nginx (reverse proxy) | 80 / 443 | Sert le frontend et reverse proxy vers backend |
| Spring Boot Backend | 8080 (interne) | API |
| PostgreSQL | 5432 | Base de données |
| RabbitMQ | 15672 (UI) / 5672 | Broker de messages |
| Memcached | 11211 | Cache |
| Elasticsearch | 9200 | Moteur de recherche |

## 🔄 Mise à jour
```bash
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d --build
```

## 📊 Logs
```bash
docker compose -f docker-compose.prod.yml logs -f
```

## 🧹 Arrêt et nettoyage
```bash
docker compose -f docker-compose.prod.yml down -v
```

## 🧩 Notes
- Le frontend et le backend partagent le même domaine grâce à Nginx.
- En prod, pas de `maildev`. Configure ton SMTP réel dans `application-prod.properties`.
- Pour SSL, place tes certificats dans `nginx/certs/` et adapte `default.conf`.
