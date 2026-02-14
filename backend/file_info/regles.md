Décision à graver dans le projet

considérer cette règle comme contractuelle :

Toute erreur liée au scope tenant (UUID, accès, ownership)
→ 403 FORBIDDEN


Et :

Toute erreur liée au format du payload utilisateur
→ 400 BAD REQUEST

Architecture cible (5.2)
security/
 ├─ TenantAuthorizationFilter.java   <-- NOUVEAU (cœur 5.2)
 ├─ SecurityConfig.java              <-- modifié
tenant/
 ├─ TenantContext.java               <-- déjà OK
 ├─ TenantResolver.java              <-- déjà OK


Les controllers ne font plus aucun check tenant.