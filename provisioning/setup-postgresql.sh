#!/bin/bash
set -e

LOG_FILE="/var/log/setup-postgres.log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "[+] Configuration des dépôts et mise à jour du système"
dnf install -y epel-release dnf-plugins-core nmap-ncat
dnf config-manager --set-enabled crb
dnf update -y --nobest --skip-broken kernel
dnf install -y kernel-devel

# Vérification des droits root
if [ "$(id -u)" -ne 0 ]; then
    echo "[!] Ce script doit être exécuté en tant que root" >&2
    exit 1
fi

# Chargement de l'environnement
ENV_FILE="/vagrant/stack.env"
TARGET="/tmp/stack.env"
rm -f "$TARGET"

if [ -f "$ENV_FILE" ]; then
    cp "$ENV_FILE" "$TARGET"
    echo "[+] stack.env copié vers $TARGET"
    source "$TARGET"
else
    echo "[!] Fichier $ENV_FILE introuvable"
    exit 1
fi

# Vérification des variables
required_vars=("POSTGRESQL_USER" "POSTGRESQL_PASSWORD" "POSTGRESQL_DATABASE" "POSTGRESQL_PORT" "POSTGRESQL_PASSWORD_ADMIN")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "[!] Variable $var non définie dans $ENV_FILE" >&2
        exit 1
    fi
done

echo "[+] Installation de PostgreSQL Server"
dnf install -y postgresql-server postgresql-contrib

# --- Vérification du répertoire de données ---
DATA_DIR="/var/lib/pgsql/data"
echo "[+] Vérification du répertoire de données: $DATA_DIR"

if [ -d "$DATA_DIR" ] && [ -n "$(ls -A "$DATA_DIR")" ]; then
    echo "   - Répertoire non vide détecté, réinitialisation..."
    systemctl stop postgresql || true
    rm -rf "${DATA_DIR}"/*
    rm -rf "${DATA_DIR}"/.??*  # Supprimer les fichiers cachés sauf . et ..
fi

# --- Initialisation de la base de données ---
echo "[+] Initialisation de la base de données PostgreSQL"
sudo -u postgres postgresql-setup --initdb || {
    echo "[!] Échec de l'initialisation, vérification des permissions"
    ls -ld "$DATA_DIR"
    ls -l "$DATA_DIR"
    exit 1
}

# Démarrage du service
echo "[+] Démarrage du service PostgreSQL"
systemctl enable --now postgresql

# Vérification du démarrage
echo "[+] Vérification du statut du service PostgreSQL"
sleep 3
if ! systemctl is-active --quiet postgresql; then
  echo "[!] Échec du démarrage de PostgreSQL" >&2
  journalctl -u postgresql --no-pager -n 20
  exit 1
fi
# Forcer SCRAM-SHA-256 pour tous les nouveaux mots de passe
echo "[+] Forçage de l'encryption SCRAM-SHA-256"
sudo -u postgres psql -c "ALTER SYSTEM SET password_encryption = 'scram-sha-256';"
sudo systemctl restart postgresql

# entré dans le dossier temps (éviter les warning)
cd /tmp

# --- Définition du mot de passe pour 'postgres' ---
echo "[+] Définition du mot de passe pour l'utilisateur postgres"
sudo -Hu postgres env HOME=/tmp psql -d postgres -c "ALTER USER postgres WITH PASSWORD '${POSTGRESQL_PASSWORD_ADMIN}';"
# Forcer listen_addresses via ALTER SYSTEM (priorité la plus haute)
echo "[+] Forçage de listen_addresses"
sudo -u postgres psql -c "ALTER SYSTEM SET listen_addresses TO '*';"

# redémarrage du service PostgreSQL
sudo systemctl restart postgresql
# Configuration des connexions distantes
echo "[+] Configuration des connexions distantes"
PG_CONF="/var/lib/pgsql/data/postgresql.conf"
PG_HBA="/var/lib/pgsql/data/pg_hba.conf"

# Configuration de l'écoute
sed -i "s/^#listen_addresses = .*/listen_addresses = '*'/" "$PG_CONF"

# Configuration des règles d'accès
sudo tee "$PG_HBA" > /dev/null <<EOT
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# Connexions locales
local   all             postgres                                peer
local   all             all                                     scram-sha-256
host    all             all             127.0.0.1/32            scram-sha-256
host    all             all             ::1/128                 scram-sha-256

# Connexions distantes
host    all             all             192.168.56.0/24         scram-sha-256
host    all             all             0.0.0.0/0               scram-sha-256
host    all             all             ::/0                    scram-sha-256

# Réplication
host    replication     all             192.168.56.0/24         scram-sha-256
host    replication     all             127.0.0.1/32            scram-sha-256
host    replication     all             ::1/128                 scram-sha-256
EOT

# vérification de la configuration
echo "[+] Vérification de la configuration PostgreSQL"
cat $PG_HBA



# Création de l'utilisateur s'il n'existe pas
sudo -Hu postgres env HOME=/tmp psql -d postgres <<EOF
DO
\$do\$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '${POSTGRESQL_USER}') THEN
      CREATE ROLE ${POSTGRESQL_USER} LOGIN PASSWORD '${POSTGRESQL_PASSWORD}';
      RAISE NOTICE 'Utilisateur % créé', '${POSTGRESQL_USER}';
   ELSE
      ALTER ROLE ${POSTGRESQL_USER} WITH PASSWORD '${POSTGRESQL_PASSWORD}';
      RAISE NOTICE 'Mot de passe de % mis à jour', '${POSTGRESQL_USER}';
   END IF;
END
\$do\$;
EOF

# Création de la base de données
sudo -Hu postgres env HOME=/tmp psql -d postgres -tc "SELECT 1 FROM pg_database WHERE datname='${POSTGRESQL_DATABASE}'" | grep -q 1 \
  || sudo -Hu postgres env HOME=/tmp psql -d postgres -c "CREATE DATABASE ${POSTGRESQL_DATABASE} OWNER ${POSTGRESQL_USER};"

# Donner les droits
sudo -Hu postgres env HOME=/tmp psql -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRESQL_DATABASE} TO ${POSTGRESQL_USER};"
sudo -Hu postgres env HOME=/tmp psql -d postgres -c "ALTER DATABASE ${POSTGRESQL_DATABASE} OWNER TO ${POSTGRESQL_USER};"

# redémarrage du service PostgreSQL
sudo systemctl restart postgresql

# Configuration du pare-feu
if command -v firewall-cmd &>/dev/null; then
    echo "[+] Configuration du pare-feu"
    systemctl enable --now firewalld
    firewall-cmd --add-port=${POSTGRESQL_PORT}/tcp --permanent
    firewall-cmd --reload
fi

echo "[+] Vérification de l'écoute sur le port ${POSTGRESQL_PORT}"
ss -tulpn | grep ${POSTGRESQL_PORT} || echo "[!] Aucune écoute détectée sur le port ${POSTGRESQL_PORT}"
echo "[✔] PostgreSQL prêt sur IP: $(hostname -I | awk '{print $1}')"