🎯 OBJECTIF MVP

Démontrer en conditions réelles :

📱 Client passe commande via site web

🍽️ Cuisine reçoit en temps réel (KDS minimal)

⏱️ Cuisine renvoie un temps d’attente

🔄 Client reçoit mises à jour statut en live

📅 Gestion réservation simple

Architecture mono-app pour la démo.

🧱 1️⃣ BACKEND — Spring Boot (Monolith)
✅ Stack

Spring Boot

Spring Web

Spring Data JPA

H2 (démo) ou PostgreSQL

WebSocket (STOMP + SimpleBroker)

Validation (Jakarta)

🗄️ Modèle de données
🔹 MenuItem

id (Long auto-incrémenté)

name

description

price

available (boolean)

🔹 Order

id (UUID)

customerName

customerPhone

type (TAKEAWAY)

status (PENDING, ACCEPTED, READY, COMPLETED, CANCELLED)

createdAt

estimatedReadyTime (LocalDateTime)

totalAmount

Relation :

List<OrderItem>

🔹 OrderItem

id

order

menuItem

quantity

priceAtOrderTime

🔹 Reservation

id (UUID)

customerName

customerPhone

reservationDateTime

numberOfGuests

status (PENDING, CONFIRMED, CANCELLED)

createdAt

📦 2️⃣ DTOs
Order
CreateOrderRequest

customerName

customerPhone

items[]

OrderResponse

id

status

totalAmount

createdAt

estimatedReadyTime

items[]

Reservation
CreateReservationRequest

customerName

customerPhone

reservationDateTime

numberOfGuests

ReservationResponse

id

status

reservationDateTime

🌐 3️⃣ REST API
📦 Orders

POST /api/orders
→ création commande

GET /api/orders/{id}
→ détail commande

PATCH /api/orders/{id}/accept?minutes=15
→ accepte + fixe temps

PATCH /api/orders/{id}/ready
→ prêt

PATCH /api/orders/{id}/complete
→ terminé

📅 Reservations

POST /api/reservations
→ créer réservation

GET /api/reservations
→ liste pour admin

PATCH /api/reservations/{id}/confirm

PATCH /api/reservations/{id}/cancel

🔁 4️⃣ MACHINE D’ÉTAT
Commande

PENDING
→ ACCEPTED
→ READY
→ COMPLETED

Annulation possible seulement depuis PENDING.

Réservation

PENDING
→ CONFIRMED
→ CANCELLED

⚡ 5️⃣ WEBSOCKET
Configuration

Endpoint :

/ws


Broker :

/topic

Topics
🔹 KDS

/topic/orders

Event :

NEW_ORDER

🔹 Client spécifique

/topic/order/{id}

Events :

ORDER_ACCEPTED

ORDER_READY

ORDER_COMPLETED

🔹 Réservations (admin)

/topic/reservations

Event :

NEW_RESERVATION

Event Wrapper standard
WebSocketEvent<T>
- type
- payload

🖥️ 6️⃣ FRONTEND — Angular (Standalone API)
🎯 3 interfaces distinctes
📱 A. Client Web

Fonctions :

Voir menu (DB statique)

Ajouter panier

Passer commande

Voir statut live

Recevoir temps d’attente

Voir “Commande prête”

Pages :

Menu

Panier

Confirmation + suivi live

Réservation

Connexion WebSocket après création commande.

🍳 B. KDS (Kitchen Display System)

Interface minimaliste tablette / laptop.

Fonctions :

Liste commandes PENDING

Bouton “Accepter”

Champ minutes

Bouton “Prête”

Mise à jour temps réel

Écran simple :
Colonnes :

Nouvelle

En préparation

Prête

🧑‍💼 C. Admin Réservations

Fonctions :

Voir liste réservations

Confirmer

Annuler

🗄️ 7️⃣ DB Démo

Menu pré-rempli (data.sql)

4–6 pizzas

Pas d’auth

Pas de paiement

Pas de multi-restaurant

🔐 8️⃣ Simplifications MVP

Mono-restaurant

Pas de login

Pas de paiement

Pas de rôles

Pas de sécurité avancée

SimpleBroker en mémoire

Déploiement Docker simple

🐳 9️⃣ Docker

Conteneurs :

backend

frontend

Optionnel :

postgres

🧠 🔟 Flow global final
Commande

Client passe commande

REST → Order created (PENDING)

WS → NEW_ORDER vers KDS

Cuisine accepte (15 min)

REST → statut ACCEPTED

WS → ORDER_ACCEPTED vers client

Cuisine clique READY

WS → ORDER_READY vers client

Réservation

Client réserve

REST → PENDING

WS → NEW_RESERVATION admin

Admin confirme

REST update

Optionnel WS vers client

📊 État du projet

Conceptuellement :

Architecture validée

Modèle défini

Endpoints définis

WebSocket défini

Flows définis

MVP cadré

Il ne reste plus que l’implémentation structurée.