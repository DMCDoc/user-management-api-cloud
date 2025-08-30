#!/bin/bash
set -e
set +x  # Désactive le debug log s'il était activé
# Script de déploiement pour l'application Spring Boot User Management API

# Vérification root
if [ "$(id -u)" -ne 0 ]; then
    echo "[!] Ce script doit être exécuté en tant que root." >&2
    exit 1
fi

# mise à jour des dépôts et du système
echo "[+] Configuration des dépôts et mise à jour du système"#dnf update -y --nobest --skip-broken
dnf upgrade -y --nobest --skip-broken
dnf install -y dnf-plugins-core
dnf install -y epel-release
dnf install -y nmap-ncat
dnf install -y unzip
dnf install -y wget
sudo dnf install -y postgresql


# Attendre que PostgreSQL soit opérationnel
echo "Vérification de la disponibilité de PostgreSQL..."
while ! nc -z vm-postgresql 5432; do
  sleep 5
  echo "En attente de PostgreSQL..."
done

# Attendre RabbitMQ
echo "Vérification de RabbitMQ..."
while ! nc -z vm-rabbit 5672; do
  sleep 5
  echo "En attente de RabbitMQ..."
done

# Chargement de l'environnement
echo "[+] Chargement de l'environnement"

ENV_FILE="/vagrant/stack.env"
TARGET="/tmp/stack.env"
rm -f "$TARGET"

if [ -f "$ENV_FILE" ]; then
    cp "$ENV_FILE" "$TARGET"
    echo "[+] stack.env copié vers $TARGET"
    set -a
    source "$TARGET"
    set +a
else
    echo "[!] Fichier $ENV_FILE introuvable"
    exit 1
fi

# Chemin du fichier JAR à déployer
APP_SOURCE=${SOURCE}
APP_DEST="/opt/backend/usermanagement-1.0.jar"

# Vérification des variables requises
required_vars=(JWT_SECRET JWT_EXPIRATION SERVER_PORT SOURCE BUILD POM_SOURCE APP_RESOURCES VM_SOURCE HTTPS_NGINX SPRING_DATASOURCE_URL SPRING_DATASOURCE_DRIVER)
missing_vars=()
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        missing_vars+=("$var")
    fi
done

if [ ${#missing_vars[@]} -ne 0 ]; then
    echo "[!] Variables manquantes dans $ENV_FILE: ${missing_vars[*]}" >&2
    exit 1
fi
echo "[+] Toutes les variables requises sont définies."
# Vérification des ressources de l'application
echo "[+] Vérification des ressources de l'application dans $VM_SOURCE"
if [  -d "$VM_SOURCE" ]; then
    echo "[~] Répertoire $VM_SOURCE existe déjà, suppression des fichiers existants"
    rm -rf "$VM_SOURCE/*"
fi

# Chemin du fichier JAR à déployer
APP_SOURCE=${SOURCE}
APP_DEST="/opt/backend/backend-1.0.0.jar"

# Suppression de l'ancien fichier JAR s'il existe
echo "[+] Suppression de l'ancien fichier JAR"
if [ -f "$APP_DEST" ]; then
    echo "[~] Fichier JAR existant trouvé, suppression"
    rm -f "$APP_DEST"
fi
# Installation de Java 21
echo "[+] Installation de Java 21"
sudo dnf install java-21-openjdk java-21-openjdk-devel -y

# Configuration de Java 21
echo "[INFO] Setting Java 21 as default..."
sudo alternatives --install /usr/bin/java java /usr/lib/jvm/java-21-openjdk/bin/java 1
sudo alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-21-openjdk/bin/javac 1
sudo alternatives --set java /usr/lib/jvm/java-21-openjdk/bin/java
sudo alternatives --set javac /usr/lib/jvm/java-21-openjdk/bin/javac

# Forcer l'utilisation de Java 21 par défaut pour Maven
echo "[+] Configuration JAVA_HOME pour Java 21"
echo "export JAVA_HOME=/usr/lib/jvm/java-21-openjdk" > /etc/profile.d/java21.sh
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile.d/java21.sh
chmod +x /etc/profile.d/java21.sh

# Vérification
echo "[+] Vérification de la configuration Java"
java -version
javac -version


# Vérification de la version Java
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [[ $JAVA_VERSION -ne 21 ]]; then
    echo "[!] Java 21 requis mais version $JAVA_VERSION détectée" >&2
    exit 1
fi

# Installation de netcat pour les vérifications réseau
echo "[+] Installation de netcat pour les vérifications réseau"
dnf install -y nc

# Création de l'utilisateur backend
echo "[+] Création de l'utilisateur backend"
if ! id backend &>/dev/null; then
    useradd -r -s /bin/false backend -d /opt/backend
    echo "[+] Utilisateur backend créé"
else
    echo "[~] Utilisateur backend existe déjà"
fi

# copie des ressources de l'application
echo "[+] Copie des ressources de l'application depuis $APP_RESOURCES vers $VM_SOURCE"
if [ -d "$APP_RESOURCES" ]; then
    if [ -d "$VM_SOURCE" ]; then
        echo "[~] Répertoire $VM_SOURCE existe déjà, suppression des fichiers existants"
        rm -rf $VM_SOURCE/*
    fi
# Création du répertoire de destination s'il n'existe pas
    echo "[+] Création du répertoire $VM_SOURCE"
    mkdir -p $VM_SOURCE
    echo "[+] Répertoire $VM_SOURCE créé"
# Copie des ressources
    cp -rp "$APP_RESOURCES"/* "$VM_SOURCE/"
    echo "[+] Ressources copiées dans $VM_SOURCE"
    fi
    echo "[+] Déplacement des fichiers .env vers /opt/backend/"
    mv /opt/env/*.env /opt/backend/
    echo "[+] Changement de propriétaire des fichiers .env"
    chown -R backend:backend $VM_SOURCE


# Permissions
echo "[+] Attribution des permissions à l'utilisateur backend"
chown -R backend: /opt/backend
chmod 750 /opt/backend
echo "[+] Vérification des permissions du répertoire /opt/backend"
ls -ld /opt/backend

# vérification de la présence de pom.xml
echo "[+] Vérification de la présence du fichier pom.xml dans $VM_SOURCE"
if [ ! -f "$VM_SOURCE/pom.xml" ]; then
    echo "[!] Fichier pom.xml introuvable dans $VM_SOURCE" >&2
    echo "[!] Veuillez vérifier le chemin du fichier POM.xml" >&2
    exit 1
fi

# Définition du fichier .env cible selon le profil

ENV_DEST="/opt/backend/${APP_ENV}.env"
echo "[+] Profil actif : $APP_ENV"
echo "[+] Fichier .env cible : $ENV_DEST"

# Génération des fichiers .env
echo "[+] Génération du fichier .env pour le profil $APP_ENV"
if [ -f "$ENV_DEST" ]; then
    echo "[~] Fichier $ENV_DEST existe déjà, suppression"
    rm -f "$ENV_DEST"
fi
cat > "$ENV_DEST" <<EOF
# === .env pour profil $APP_ENV ===
APP_ENV=$APP_ENV
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION=$JWT_EXPIRATION
SERVER_PORT=$SERVER_PORT
SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL
SPRING_DATASOURCE_DRIVER=$SPRING_DATASOURCE_DRIVER
EOF

chmod 600 "$ENV_DEST"
chown backend: "$ENV_DEST"
echo "[+] Fichier $ENV_DEST généré avec succès."
ls -l "$ENV_DEST"


# Vérification des droits et propiété
echo "[+] Vérification des droits et de la propriété des ressources"
ls -ld /opt
sleep 1
ls -ld /opt/backend
sleep 1
ls -ld /opt/backend/*
sleep 1

# vérification des fichiers .env
echo "[+] Vérification des fichiers .env"
if [ ! -f "$ENV_DEST" ]; then
    echo "[!] Fichier .env introuvable : $ENV_DEST" >&2
    exit 1
fi
cat "$ENV_DEST"

# Vérification de la connexion à la base de données postgresql
echo "[+] Vérification de la connexion à la base de données"
if ! nc -z 192.168.56.15 5432; then
    echo "[!] PostgreSQL n'est pas accessible sur 192.168.56.15:5432" >&2
    echo "[!] Veuillez vérifier que le service PostgreSQL est en cours d'exécution"
else
    echo "[+] PostgreSQL est accessible"
fi
sleep 3
# Après la génération du fichier .env
echo "[+] Exportation des variables pour Java"
set -a
source /opt/backend/${APP_ENV}.env
set +a

# Vérification
echo "[+] Vérification des variables exportées"
echo "JWT_SECRET=${JWT_SECRET}"
echo "SERVER_PORT=${SERVER_PORT}"


# Affichage du contenu du fichier .env
echo "[+] Affichage du contenu de $ENV_DEST :"
cat "$ENV_DEST"

# installation de maven manuellement
echo "[+] Installation de Maven"
    wget https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
    tar -xvzf apache-maven-3.9.11-bin.tar.gz -C /opt/
    ln -s /opt/apache-maven-3.9.11 /opt/maven
    echo "export PATH=/opt/maven/bin:\$PATH" | sudo tee /etc/profile.d/maven.sh
    source /etc/profile.d/maven.sh
    chmod +x /etc/profile.d/maven.sh
    source /etc/profile.d/maven.sh
    echo "[+] Vérification de la version de Maven"
    mvn -v

# Vérification de Maven
echo "[+] Vérification de Maven"
if ! command -v mvn &> /dev/null; then
    echo "[~] Maven non trouvé, installation en cours..."
    dnf install -y maven
    echo "[+] Maven installé avec succès."
else
    echo "[~] Maven est déjà présent : $(mvn -v | head -n 1)"
fi

# Compilation de l'application si BUILD est vrai
echo "[+] Compilation de l'application"
if [ "$BUILD" = true ]; then
    echo "[+] Compilation Maven dans la VM"
    cd "/opt/" # Assurez-vous que le chemin est correct
    mvn package spring-boot:repackage -DskipTests
    cp $SOURCE "$APP_DEST"
fi
# Attribution des permissions au fichier JAR
echo "[+] Attribution des permissions au fichier JAR"
chown backend:backend "$APP_DEST"
chmod 750 "$APP_DEST"
echo "[+] Vérification des permissions du fichier JAR"
ls -l "$APP_DEST"

# Configuration de la journalisation
echo "[+] Configuration de la rotation des logs"
cat > /etc/logrotate.d/backend <<EOF
/var/log/backend.log
/var/log/backend-error.log {
    daily
    rotate 7
    missingok
    compress
    delaycompress
    notifempty
    create 0640 backend backend
}
EOF

# Verification du chemin réel de Java
echo "[+] Vérification du chemin de Java"
REAL_JAVA_PATH=$(readlink -f $(which java))
echo "Chemin réel de Java: $REAL_JAVA_PATH"

# Permission du binaire Java à backend
echo "[+] Attribution des permissions du binaire Java à l'utilisateur backend"
chown backend:backend $REAL_JAVA_PATH
chmod 755 $REAL_JAVA_PATH

# Vérification les permissions du binaire Java
echo "[+] Vérification des permissions du binaire Java"
ls -l $REAL_JAVA_PATH

# Vérification que Java est exécutable par tous
echo "[+] Vérification que Java est exécutable par tous"
if [ ! -x /usr/bin/java ]; then
    echo "[!] Java n'est pas exécutable par tous" >&2
    echo "[~] Correction des permissions de Java"
    chmod 755 /usr/bin/java
else
    echo "[~] Java est déjà exécutable par tous"
fi

# Vérification de SELinux
echo "[+] Vérification de SELinux"
if command -v sestatus >/dev/null; then
    sestatus
    if sestatus | grep -q "enabled"; then
        echo "[~] SELinux est activé, tentative de désactivation temporaire"
        setenforce 0
        echo "[+] SELinux mis en mode permissif"
    fi
fi

# Correction des permissions des fichiers log
echo "[+] Correction des permissions des fichiers de log"
touch /var/log/backend.log /var/log/backend-error.log
chown backend:backend /var/log/backend*.log
chmod 640 /var/log/backend*.log

# Création du service systemd
echo "[+] Création du service systemd avec configuration complète"
cat > /etc/systemd/system/backend.service <<EOF
[Unit]
Description=Spring Boot Backend Service
After=network.target mysql.service rabbitmq-server.service
Wants=mysql.service rabbitmq-server.service

[Service]
User=backend
Group=backend
WorkingDirectory=/opt/backend

# Variables d'environnement
Environment="SPRING_PROFILES_ACTIVE=dev"
Environment="JWT_SECRET=ryC7jo+dRm+je+WH8wObgDbPRTMslE3P+APNl5he7aBqrwnnCM6nvxULrGeROgV8f3X4evcGiipvTQpngQUG6g"
Environment="JWT_EXPIRATION=3600000"

# Commande de lancement
ExecStart=/usr/lib/jvm/java-21-openjdk/bin/java -jar /opt/backend/backend-1.0.0.jar

# Temps maximum pour démarrer (2 minutes)
TimeoutStartSec=120

# Restart automatique
SuccessExitStatus=143
Restart=always
RestartSec=5
StartLimitInterval=60s
StartLimitBurst=3

# Limites ressources
MemoryMax=2048M
MemoryHigh=384M
# CPUQuota=50%

[Install]
WantedBy=multi-user.target



EOF

# Vérifiction des permissions du fichier de service
echo "[+] Vérification des permissions du fichier de service"
chown backend:backend /usr/lib/jvm/java-21-openjdk-21.0.8.0.9-1.el9.x86_64/bin/java
chmod 755 /usr/lib/jvm/java-21-openjdk-21.0.8.0.9-1.el9.x86_64/bin/java
ls -l /usr/lib/jvm/java-21-openjdk-21.0.8.0.9-1.el9.x86_64/bin/java
stat /usr/bin/java

# Vérification finale des permissions
echo "[+] Vérification finale des permissions"

ls -ld /opt/backend
ls -l /opt/backend/backend-1.0.0.jar
ls -l /var/log/backend*

# Test manuel du démarrage
#echo "[+] Test manuel du démarrage en tant que 'backend'"
#sudo -u backend sh -c "$REAL_JAVA_PATH -jar /opt/backend/usermanagement-1.0.jar"

# Vérification des variables pour Spring Boot
echo "[+] Vérification des variables pour Spring Boot"
echo "SPRING_DATASOURCE_URL: $SPRING_DATASOURCE_URL"
echo "SPRING_DATASOURCE_DRIVER_CLASS_NAME: $SPRING_DATASOURCE_DRIVER_CLASS_NAME"

# Vérification de la configuration
echo "[+] Vérification finale de la configuration"
echo "Contenu du service systemd :"
cat /etc/systemd/system/backend.service

# Vérification des logs d'erreur
echo "[+] Consultation des logs d'erreur"
cat /var/log/backend-error.log

# Recharger systemd
echo "[+] Reload systemd"
systemctl daemon-reload

# Activation du service
echo "[+] Activation du service backend.service"

# Activer le service pour qu'il démarre au boot
echo "[+] Enable backend.service"
systemctl enable backend.service

# Démarrer le service
systemctl start backend.service
sleep 5  # Donne un peu de temps pour que le service démarre

# Vérification des logs du service
 echo "[+] Vérification des logs du service"
 journalctl -u backend.service -n 10 --no-pager

# Démarrage du firewall si nécessaire
echo "[+] Démarrage du firewall si nécessaire"
if command -v systemctl &>/dev/null && systemctl is-active --quiet firewalld; then
    echo "[~] Firewalld est déjà actif"
else
    echo "[+] Démarrage de firewalld"
    systemctl start firewalld
    systemctl enable firewalld
    echo "[+] Firewalld démarré et activé"
fi

# ouverture des ports
echo "[+] Ouverture du port ${SERVER_PORT};${HTTPS_PORT} dans le pare-feu"
if command -v firewall-cmd &>/dev/null && firewall-cmd --state &>/dev/null; then
    firewall-cmd --add-port=${SERVER_PORT}/tcp --permanent
    firewall-cmd --add-port=${HTTPS_NGINX}/tcp --permanent
    firewall-cmd --reload
    echo "[+] Port ${SERVER_PORT};${HTTPS_NGINX} ouverts"
else
    echo "[~] Pare-feu non actif, aucune modification"
fi

# Vérification de l'état du service backend
echo "[+] Vérification de l'état du service backend"
if ! systemctl is-active --quiet backend; then
  echo "[!] Le service Backend n'a pas démarré" >&2
  exit 1
fi

# Vérification de l'état de santé de l'application
response=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:${SERVER_PORT}/actuator/health")
if [ "$response" = "200" ]; then
    echo "[✔] Backend opérationnel (HTTP 200)"
else
    echo "[!] Backend non accessible ou protégé (HTTP $response)"
    exit 1
fi



# Vérification finale avec gestion d'erreur
echo "[+] Vérification finale de l'application"
response=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:${SERVER_PORT}/actuator/health")
if [ "$response" = "200" ]; then
    echo "[✔] Backend opérationnel (HTTP 200)"
else
    echo "[!] Problème de santé de l'application (HTTP $response)" >&2
    journalctl -u backend --no-pager -n 30
    exit 1
fi

 echo "[✔] Backend Spring Boot opérationnel sur http://$(hostname -I | awk '{print $1}'):${SERVER_PORT}"
echo "     Profil: dev | Port: ${SERVER_PORT} | Health Check: /actuator/health" 