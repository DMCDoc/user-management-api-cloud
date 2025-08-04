#!/bin/bash

set -e
# revenir au répertoire racine du projet
cd ..
cd backend/usermanagement
# script pour construire et déployer le backend Spring Boot
echo "📦 Compilation du backend Spring Boot..."
mvn clean install spring-boot:repackage
# vérifier si la compilation a réussi
if [ $? -ne 0 ]; then
  echo "❌ Échec de la compilation du backend Spring Boot."
  exit 1
fi
echo "✅ Compilation réussie."
# construire et deployer le frontend
echo "📦 Construction du frontend..."
cd ../../frontend/user-client-javafx
mvn clean install
# vérifier si la compilation a réussi
if [ $? -ne 0 ]; then
  echo "❌ Échec de la compilation du frontend."
  exit 1
fi
echo "✅ Construction du frontend réussie."
# revenir au répertoire racine du projet
cd ../..

echo "✅ JAR généré avec succès dans target/"

echo "🚀 Lancement de la VM userbackend avec provisionnement"
vagrant up --provision
