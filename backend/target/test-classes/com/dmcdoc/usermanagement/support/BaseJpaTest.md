3️⃣ EXPLICATION DÉTAILLÉE (le pourquoi du comment)

On décortique ligne par ligne, pédagogiquement.

🔹 @DataJpaTest
@DataJpaTest


👉 Démarre uniquement :

JPA

Hibernate

Repositories

H2

❌ Ne démarre PAS :

sécurité

web

controllers

filtres HTTP

💡 Parfait pour tester la couche persistence, mais :
➡️ le multi-tenant n’est PAS automatique

🔹 @ActiveProfiles("test")
@ActiveProfiles("test")


👉 Force :

application-test.yml

H2

config spécifique test

💡 Indispensable pour :

éviter PostgreSQL

éviter Testcontainers à ce stade

🔹 @Import(HibernateTenantFilterConfig.class)
@Import({
    HibernateTenantFilterConfig.class
})


👉 LIGNE CLÉ DU MULTI-TENANT

Sans ça :

Hibernate démarre

MAIS le filtre tenant n’est jamais enregistré

tes tests passent sans isolation

💣 Faux tests = dette technique


👉 Simule exactement :

un header X-Tenant-ID

un resolver tenant

un contexte utilisateur réel

💡 Tous les repositories héritant de ce test :

sont automatiquement tenant-aware

échouent s’ils ignorent le tenant

🔹 TenantContext.clear()
@AfterEach
void clearTenantContext() {
    TenantContext.clear();
}


👉 Très important pour :

éviter les fuites entre tests

garantir l’indépendance des tests

préparer l’exécution parallèle

⚠️ Sans ça :

certains tests passent par hasard

d’autres échouent de manière aléatoire

4️⃣ Pourquoi PAS utiliser le bypass ici ?

Tu pourrais faire ceci :

TenantContext.enableBypass();


❌ MAIS ce serait une erreur pédagogique pour les tests repository.

Pourquoi ?

Le bypass court-circuite toute la logique tenant

Les tests passeraient même si :

le filtre Hibernate est cassé

une requête cross-tenant fuit

👉 On veut l’inverse : des tests qui cassent fort.