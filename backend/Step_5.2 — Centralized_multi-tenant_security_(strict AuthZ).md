Étape 5.2 — Sécurité multi-tenant centralisée (AuthZ stricte)

Objectif : sortir définitivement la logique tenant des controllers et la faire respecter au niveau sécurité, de manière homogène et testable.

Principe retenu (clair et non négociable)

Le tenant effectif vient du JWT

Le header X-Tenant-ID est optionnel

Si header présent :

il doit matcher le tenant du JWT

sinon 403

SUPER_ADMIN :

bypass total

aucun tenant requis

USER / TENANT_ADMIN :

tenant obligatoire

mismatch → 403

Toute requête invalide → 403, jamais 400