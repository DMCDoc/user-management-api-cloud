#!/bin/bash
set -e

# Redirection des logs vers un fichier
LOG_FILE="/var/log/setup-mysql.log"
exec > >(tee -a "$LOG_FILE") 2>&1


echo "[+] Configuration des dépôts et mise à jour du système"
dnf install -y epel-release
dnf install -y dnf-plugins-core
dnf config-manager --set-enabled crb
dnf update -y --nobest --skip-broken kernel
sudo dnf install -y nmap-ncat

echo "[+] Installation du kernel-devel"
dnf install -y kernel-devel

# Vérification des droits root
if [ "$(id -u)" -ne 0 ]; then
    echo "[!] Ce script doit être exécuté en tant que root" >&2
    exit 1
fi

# Chargement de l'environnement
echo "[+] Chargement de l'environnement"

ENV_FILE="/vagrant/stack.env"
TARGET="/tmp/stack.env"
rm -f "$TARGET"

if [ -f "$ENV_FILE" ]; then
    cp "$ENV_FILE" "$TARGET"
    echo "[+] stack.env copié vers $TARGET"
    source "$TARGET"
else
    echo "[!] Fichier $ENV_FILE introuvable"
fi

# Vérifier que les variables requises existent
required_vars=("MYSQL_ROOT_PASSWORD" 
  "MYSQL_USER" 
  "MYSQL_PASSWORD" 
  "MYSQL_DATABASE" 
  "MYSQL_PORT")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "[!] Variable $var non définie dans $ENV_FILE" >&2
        exit 1
    fi
done

# Installation de MySQL Server
echo "[+] Installation de MySQL Server"
dnf install -y mysql-server

echo "[+] Activation et démarrage du service MySQL"
systemctl enable --now mysqld


# Attendre que MySQL soit opérationnel
echo "[+] Attente du démarrage de MySQL..."
until mysqladmin ping &>/dev/null; do
    sleep 1
done

echo "[+] Sécurisation initiale de MySQL"
# Modification majeure: Adaptation pour les versions récentes
mysql --connect-expired-password <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';
DELETE FROM mysql.user WHERE User='';
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');
DROP DATABASE IF EXISTS test;
FLUSH PRIVILEGES;
EOF

echo "[+] Configuration de MySQL pour accepter les connexions distantes"
cp /etc/my.cnf /etc/my.cnf.bak
sed -i 's/^bind-address/#bind-address/' /etc/my.cnf

echo "[+] Redémarrage du service MySQL"
systemctl restart mysqld

echo "[+] Création de la base de données et de l’utilisateur"
# Modification: Utilisation du nouveau mot de passe root
mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<EOF
CREATE DATABASE IF NOT EXISTS $MYSQL_DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD'; # Mot de passe $MYSQL_PASSWORD
GRANT ALL PRIVILEGES ON $MYSQL_DATABASE.* TO '$MYSQL_USER'@'%';
FLUSH PRIVILEGES;
EOF

# Ajout: Configuration du pare-feu
echo "[+] Configuration du pare-feu"
# Assurez-vous que firewalld est installé
MYSQL_PORT=${MYSQL_PORT:-3306}

if command -v firewall-cmd &>/dev/null; then
    systemctl enable --now firewalld
    echo "[+] Firewalld activé"
    firewall-cmd --add-port=$MYSQL_PORT/tcp --permanent
    firewall-cmd --reload
fi

# Vérification de l'état du service MySQL
if ! systemctl is-active --quiet mysqld; then
  echo "[!] Le service MySQL n'a pas démarré" >&2
  exit 1
fi

# Vérification de l'écoute sur le port 3306
echo "[+] Vérification de l'écoute sur le port $MYSQL_PORT"
ss -tulpn | grep $MYSQL_PORT
echo "[✔] MySQL prêt sur port $MYSQL_PORT pour IP : $(hostname -I | awk '{print $1}')"



