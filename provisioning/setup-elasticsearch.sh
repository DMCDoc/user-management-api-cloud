#!/bin/bash
set -e
export PATH=$PATH:/usr/share/elasticsearch/bin

# Mise à jour du système
echo "[+] Mise à jour du système"
dnf update -y --nobest --skip-broken kernel
dnf upgrade -y
dnf install -y epel-release
dnf config-manager --set-enabled crb
dnf install -y nmap-ncat

# mise à jour de la version
if [[ "$1" == "--version" && -n "$2" ]]; then
    echo "[+] Mise à jour de la version dans $ENV_FILE"
    sed -i "s/^ELASTIC_VERSION=.*/ELASTIC_VERSION=$2/" "$ENV_FILE"
    echo "[✔] Version changée → $2"
    exit 0
fi

# Vérification des droits root
if [ "$(id -u)" -ne 0 ]; then
    echo "[!] Ce script doit être exécuté en tant que root" >&2
    exit 1
fi

# Configuration de la locale système
echo "[+] Configuration de la locale pour éviter les warnings Java"
localectl set-locale LANG=en_US.UTF-8

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

# Vérification des variables nécessaires
required_vars=(ELASTIC_VERSION ELASTIC_HTTP_PORT ELASTIC_TRANSPORT_PORT ELASTIC_HEAP_SIZE)
missing_vars=()
for var in "${required_vars[@]}"; do
    [[ -z "${!var}" ]] && missing_vars+=("$var")
done

if [[ ${#missing_vars[@]} -gt 0 ]]; then
    echo "[!] Variables manquantes dans $ENV_FILE:" >&2
    printf " - %s\n" "${missing_vars[@]}" >&2
    exit 1
fi

# Vérification de java
if ! command -v java &>/dev/null; then
    echo "[+] Installation de Java (openjdk11)"
    dnf install -y java-11-openjdk
fi

echo "[+] Ajout du dépôt Elasticsearch"
rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch

ELASTIC_MAJOR="${ELASTIC_VERSION%%.*}"
cat > /etc/yum.repos.d/elasticsearch.repo <<EOF
[elasticsearch-${ELASTIC_MAJOR}]
name=Elasticsearch repository for ${ELASTIC_MAJOR}.x packages
baseurl=https://artifacts.elastic.co/packages/${ELASTIC_MAJOR}.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=1
autorefresh=1
type=rpm-md
EOF

echo "[+] Installation de Elasticsearch ${ELASTIC_VERSION}"
if ! dnf list installed "elasticsearch-${ELASTIC_VERSION}" &>/dev/null; then
    dnf install -y "elasticsearch-${ELASTIC_VERSION}"
else
    echo "[~] Elasticsearch ${ELASTIC_VERSION} déjà installé"
fi

echo "[+] Configuration personnalisée"
CONF_FILE="/etc/elasticsearch/elasticsearch.yml"
BACKUP_FILE="${CONF_FILE}.bak-$(date +%Y%m%d%H%M%S)"
cp "$CONF_FILE" "$BACKUP_FILE"

echo "[+] Sauvegarde de la configuration originale dans $BACKUP_FILE"
cat > "$CONF_FILE" <<EOF
cluster.name: ${CLUSTER_NAME:-dev-cluster}
node.name: ${NODE_NAME:-$(hostname)}
network.host: 0.0.0.0
network.publish_host: ${PUBLISH_HOST:-_local_}
http.port: ${ELASTIC_HTTP_PORT}
transport.port: ${ELASTIC_TRANSPORT_PORT}
path.logs: /var/log/elasticsearch
path.data: /usr/share/elasticsearch/data
discovery.type: single-node
xpack.security.enabled: false
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
EOF
echo "[✔] Configuration sauvegardée dans $BACKUP_FILE"

echo "[+] Configuration de la JVM"
JVM_OPTS_DIR="/etc/elasticsearch/jvm.options.d"
mkdir -p "$JVM_OPTS_DIR"
cat > "${JVM_OPTS_DIR}/heap.options" <<EOF
-Xms${ELASTIC_HEAP_SIZE}
-Xmx${ELASTIC_HEAP_SIZE}
EOF

echo "[+] Validation des permissions"

# Dossier logs
LOG_DIR="/var/log/elasticsearch"
mkdir -p "$LOG_DIR"
chown -R elasticsearch:elasticsearch "$LOG_DIR"
chmod 750 "$LOG_DIR"

# Dossier data
DATA_DIR="/usr/share/elasticsearch/data"
mkdir -p "$DATA_DIR"
chown -R elasticsearch:elasticsearch "$DATA_DIR"
chmod 750 "$DATA_DIR"

# Lancement de Elasticsearch
ELASTIC_BIN="/usr/share/elasticsearch/bin/elasticsearch"
if ! su -s /bin/bash elasticsearch -c "$ELASTIC_BIN -d -p /tmp/elastic-pid"; then
    sleep 2
    if [[ -f /tmp/elastic-pid ]] && ! kill -0 $(cat /tmp/elastic-pid) 2>/dev/null; then
        echo "[!] Configuration Elasticsearch invalide. Vérifiez les logs." >&2
        exit 1
    fi
    [[ -f /tmp/elastic-pid ]] && kill $(cat /tmp/elastic-pid)
fi
rm -f /tmp/elastic-pid
echo "[✔] Configuration validée"

echo "[+] Attente de la disponibilité HTTP (${ELASTIC_HTTP_PORT})"
timeout=120
start_time=$(date +%s)
while :; do
    if curl -sS "http://localhost:${ELASTIC_HTTP_PORT}" 2>/dev/null | grep -q "cluster_name"; then
        break
    fi
    
    if (( $(date +%s) - start_time > timeout )); then
        echo "[!] Timeout après ${timeout}s - Elasticsearch non disponible" >&2
        journalctl -u elasticsearch --no-pager -n 20
        exit 1
    fi
    
    sleep 5
done
# Activation du firewall
echo Activation du firewall
if ! systemctl is-active --quiet firewalld; then
    systemctl start firewalld
    systemctl enable firewalld
    echo "[✔] Firewalld démarré et activé"
else
    echo "[~] Firewalld déjà actif"
fi

# Pare-feu
if command -v firewall-cmd &>/dev/null && firewall-cmd --state &>/dev/null; then
    echo "[+] Ouverture des ports Elasticsearch"
    firewall-cmd --add-port=${ELASTIC_HTTP_PORT}/tcp --permanent
    firewall-cmd --add-port=${ELASTIC_TRANSPORT_PORT}/tcp --permanent
    firewall-cmd --reload
    echo "[✔] Ports ${ELASTIC_HTTP_PORT} et ${ELASTIC_TRANSPORT_PORT} ouverts"
else
    echo "[~] Pare-feu non actif - skip"
fi

# Limites système
echo "[+] Vérification des limites système"
sysctl -w vm.max_map_count=262144
echo 'vm.max_map_count=262144' > /etc/sysctl.d/99-elasticsearch.conf

# Limites utilisateur
echo "[+] Vérification des limites utilisateur"
ulimit -a >> /var/log/elasticsearch/ulimit.log 2>/dev/null

echo "[+] Vérification finale:"
curl -s "http://localhost:${ELASTIC_HTTP_PORT}/_cluster/health?pretty" > /var/log/elasticsearch/health_check.json

echo "[✔] Elasticsearch opérationnel sur http://$(hostname -I | awk '{print $1}'):${ELASTIC_HTTP_PORT}"
