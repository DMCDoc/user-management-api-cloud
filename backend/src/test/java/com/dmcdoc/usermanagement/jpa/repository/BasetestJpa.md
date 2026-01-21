Parfait. On part sur Restaurant et on fait le test de référence. Ce test servira ensuite de template strict pour tous les autres repositories. Pas de dispersion.

1. Objectif du test RestaurantRepositoryIT

Valider exclusivement la couche JPA pour une entité tenant-aware.

Ce test doit prouver que :

Un Restaurant ne peut pas être persisté sans tenant

Les données sont isolées par tenant

findById respecte le tenant

Les opérations CRUD n’impactent que le tenant courant

Aucun HTTP. Aucune sécurité. Aucune magie.