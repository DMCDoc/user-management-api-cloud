#!/bin/bash
set -e

# Ajout: Vérification des privilèges root
if [ "$(id -u)" -ne 0 ]; then
    echo "[!] Ce script doit être exécuté en tant que root" >&2
    exit 1
fi

# mise à jour du système
echo "[+] Mise à jour du système"
dnf install -y epel-release
dnf update -y
dnf install -y wget
echo "[+] Installation de Memcached"
dnf install -y memcached

# Ajout: Installation de libmemcached pour les clients
# Cela permet d'utiliser des clients comme `memcached-tool` ou `libmemcached`
echo "[+] Installation de libmemcached"
dnf --enablerepo=crb install 389-ds-base-devel -y
# Ajout: Vérification de l'installation
if ! command -v memcached &>/dev/null; then
    echo "[!] Échec de l'installation de Memcached" >&2
    exit 1
fi


echo "[+] Activation et démarrage de Memcached"
systemctl enable --now memcached

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

# Vérification améliorée des variables
declare -A config_vars=(
    ["MEMCACHED_MEMORY"]="64"
    ["MEMCACHED_LISTEN"]="127.0.0.1"
    ["MEMCACHED_PORT"]="11211"
)

for var in "${!config_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "[~] Utilisation valeur par défaut pour $var: ${config_vars[$var]}"
        declare "$var"="${config_vars[$var]}"
    fi
done

echo "[+] Configuration personnalisée de Memcached"
CONF_FILE="/etc/sysconfig/memcached"
cp "$CONF_FILE" "${CONF_FILE}.bak-$(date +%Y%m%d%H%M%S)"

# -v pour logging verbeux en dev
cat > "$CONF_FILE" <<EOF
PORT="${MEMCACHED_PORT}"
USER="memcached"
MAXCONN="1024"
CACHESIZE="${MEMCACHED_MEMORY}"
OPTIONS="-l ${MEMCACHED_LISTEN} -v"  
EOF

echo "[+] Fichier de configuration créé : $CONF_FILE"
echo "[+] Vérification de la configuration"
cat "$CONF_FILE"
echo "[+] Redémarrage du service"
systemctl restart memcached

# Ajout: Vérification que le service a bien redémarré
if ! systemctl is-active --quiet memcached; then
    echo "[!] Échec du démarrage de Memcached" >&2
    journalctl -u memcached -n 20 --no-pager
    exit 1
fi

# Démarrage du firewall si nécessaire
if ! systemctl is-active --quiet firewalld; then
    echo "[+] Démarrage du service firewalld"
    systemctl start firewalld
    systemctl enable firewalld
else
    echo "[~] Service firewalld déjà actif"
fi

# Pare-feu conditionnel amélioré
if command -v firewall-cmd &>/dev/null && firewall-cmd --state &>/dev/null; then
    echo "[+] Ouverture du port ${MEMCACHED_PORT} dans le pare-feu"
    firewall-cmd --add-port="${MEMCACHED_PORT}/tcp" --permanent
    firewall-cmd --reload
else
    echo "[~] Pare-feu non actif - skip"
fi

# Vérification améliorée
echo "[+] Vérification :"
if ss -tulpn | grep -q ":${MEMCACHED_PORT} "; then
    echo "[+] Memcached écoute sur ${MEMCACHED_LISTEN}:${MEMCACHED_PORT}"
else
    echo "[!] Échec de l'écoute sur le port ${MEMCACHED_PORT}" >&2
    exit 1
fi

# Ajout: Test de connexion basique
if command -v nc &>/dev/null; then
    echo -e "stats\nquit" | nc ${MEMCACHED_LISTEN} ${MEMCACHED_PORT} | head -4
fi

# Avertissement si écoute sur toutes les interfaces
if [[ "$MEMCACHED_LISTEN" == "0.0.0.0" ]]; then
    echo "[~] ⚠ Attention : écoute sur toutes les interfaces activée !"
fi


echo "[✔] Memcached prêt sur ${MEMCACHED_LISTEN}:${MEMCACHED_PORT}"