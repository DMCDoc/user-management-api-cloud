#!/bin/bash
set -e

# Vérification root
if [ "$(id -u)" -ne 0 ]; then
    echo "[!] Ce script doit être exécuté en tant que root." >&2
    exit 1
fi

# Sauvegarde de l'état SELinux original
SELINUX_STATE=$(getenforce)

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
    echo "[!] Fichier $ENV_FILE introuvable" >&2
    exit 1
fi

# Vérification des variables requises
required_vars=(BACKEND_IP BACKEND_PORT NGINX_PORT LISTEN_PORT)
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "[!] Variable manquante : $var" >&2
        exit 1
    fi
done

# Désactivation temporaire de SELinux (mode permissif)
echo "[+] Configuration temporaire de SELinux (mode permissif)"
setenforce 0 || echo "[~] Avertissement : impossible de désactiver SELinux"

echo "[+] Installation de NGINX et dépendances"
dnf update -y --nobest --skip-broken
dnf upgrade -y --nobest --skip-broken
dnf install -y dnf-plugins-core
dnf install -y epel-release
dnf install -y nginx openssl
dnf install -y policycoreutils-python-utils
dnf install -y vim
sudo dnf install -y nmap-ncat

echo "[+] Configuration des répertoires"
mkdir -p /etc/nginx/ssl /etc/nginx/snippets
chmod 700 /etc/nginx/ssl

echo "[+] Génération des certificats auto-signés"
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/nginx-selfsigned.key \
    -out /etc/nginx/ssl/nginx-selfsigned.crt \
    -subj "/CN=localhost" >/dev/null 2>&1

openssl dhparam -out /etc/nginx/ssl/dhparam.pem 2048 >/dev/null 2>&1

echo "[+] Création des snippets de configuration"
# Configuration SSL
cat > /etc/nginx/snippets/ssl-params.conf <<EOF
ssl_protocols TLSv1.2 TLSv1.3;
ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
ssl_prefer_server_ciphers on;
ssl_session_cache shared:SSL:10m;
ssl_session_timeout 1h;
ssl_session_tickets off;
ssl_dhparam /etc/nginx/ssl/dhparam.pem;
EOF

# Configuration des en-têtes proxy
cat > /etc/nginx/snippets/proxy-headers.conf <<EOF
proxy_set_header Host \$host;
proxy_set_header X-Real-IP \$remote_addr;
proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto \$scheme;
proxy_set_header Upgrade \$http_upgrade;
proxy_set_header Connection \$connection_upgrade;
proxy_http_version 1.1;
EOF

# Configuration du reverse proxy
echo "[+] Configuration du serveur NGINX"
cat > /etc/nginx/conf.d/reverse-proxy.conf <<EOF
# Redirection HTTP vers HTTPS
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name userapp.local;

    return 301 https://\$host:\$server_port\$request_uri;
}

# HTTPS + Reverse proxy vers backend Spring Boot
server {
    listen ${NGINX_PORT} ssl http2;
    listen [::]:${NGINX_PORT} ssl http2;
    server_name userapp.local;

    ssl_certificate /etc/nginx/ssl/nginx-selfsigned.crt;
    ssl_certificate_key /etc/nginx/ssl/nginx-selfsigned.key;
    include snippets/ssl-params.conf;

    client_max_body_size 10m;
    keepalive_timeout 75s;

    access_log /var/log/nginx/proxy-access.log;
    error_log /var/log/nginx/proxy-error.log;

    location / {
        proxy_pass http://${BACKEND_IP}:${BACKEND_PORT}/;
        include snippets/proxy-headers.conf;

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        send_timeout 60s;
    }

    location ~ /\.(?!well-known).* {
        deny all;
    }

    location /nginx-health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF

echo "[+] Vérification et insertion du bloc map dans nginx.conf"
nginx_conf="/etc/nginx/nginx.conf"
http_block_start=$(grep -n 'http {' "$nginx_conf" | cut -d: -f1)

if ! grep -q '\$connection_upgrade' "$nginx_conf"; then
    echo "[~] Ajout de la directive map \$http_upgrade"
    sed -i "${http_block_start}a\\
    map \$http_upgrade \$connection_upgrade {\n\
        default upgrade;\n\
        ''      close;\n\
    }\n" "$nginx_conf"
fi

echo "[+] Sécurisation de la configuration"
# Désactivation de la version serveur
sed -i 's/# server_tokens off;/server_tokens off;/' /etc/nginx/nginx.conf

# Protection des fichiers de configuration
chmod 640 /etc/nginx/conf.d/*.conf
chmod 600 /etc/nginx/ssl/*
chmod 640 /etc/nginx/snippets/*.conf

# Configuration SELinux persistante
echo "[+] Configuration permanente de SELinux"
if command -v semanage &> /dev/null; then
    # Autorisation des ports personnalisés
    semanage port -a -t http_port_t -p tcp ${LISTEN_PORT} || echo "[~] Port ${LISTEN_PORT} déjà autorisé ou erreur"
    
    # CORRECTION 1: Autoriser NGINX à se connecter au réseau
    setsebool -P httpd_can_network_connect 1
    
    # Appliquer les contextes aux fichiers
    restorecon -Rv /etc/nginx /var/log/nginx /var/lib/nginx
else
    echo "[!] semanage non installé - configuration SELinux incomplète" >&2
fi

# Réactivation de SELinux
echo "[+] Réactivation de SELinux (état original: ${SELINUX_STATE})"
setenforce "${SELINUX_STATE}"

# Vérification de la configuration NGINX
echo "[+] Vérification de la configuration"
nginx -t || {
    echo "[!] Erreur dans la configuration NGINX" >&2
    exit 1
}

echo "[+] Activation et démarrage de NGINX"
systemctl enable --now nginx
if ! systemctl is-active --quiet nginx; then
    echo "[!] NGINX n'a pas démarré correctement" >&2
    journalctl -u nginx --no-pager | tail -n 50
    exit 1
fi
echo "[✔] NGINX opérationnel"

echo "[+] Configuration du monitoring"
# CORRECTION 2: Autoriser l'accès depuis localhost
cat > /etc/nginx/conf.d/status.conf <<EOF
server {
    listen ${LISTEN_PORT};
    server_name localhost;

    location /nginx_status {
        stub_status;
        access_log off;
        allow 127.0.0.1;
        allow ::1;
        allow 192.168.56.0/24;
        deny all;
    }
}
EOF
# Démarrage du firewall si nécessaire
echo "[+] Démarrage du firewall"
if ! systemctl is-active --quiet firewalld; then
    systemctl start firewalld
    systemctl enable firewalld
    echo "[✔] Firewalld démarré"
else
    echo "[~] Firewalld déjà actif"
fi
nginx -t && systemctl reload nginx

echo "[+] Configuration du pare-feu"
if systemctl is-active --quiet firewalld; then
    echo "[~] Firewalld actif - ouverture des ports"
    firewall-cmd --add-port=${NGINX_PORT}/tcp --permanent
    firewall-cmd --add-port=${LISTEN_PORT}/tcp --permanent
    firewall-cmd --reload
    echo "[+] Ports ouverts: ${NGINX_PORT}, ${LISTEN_PORT}"
else
    echo "[~] Firewalld non actif - aucune modification"
fi

# Tests de validation
echo "[+] Démarrage des tests de validation"

echo "[1] Vérification redirection HTTP -> HTTPS"
curl -k -I http://localhost/ 2>/dev/null | grep -q "301 Moved Permanently" \
  && echo "[✔] Redirection HTTP vers HTTPS fonctionnelle" \
  || echo "[!] Erreur : redirection HTTP vers HTTPS échouée"

echo "[2] Test endpoint de santé NGINX"
curl -k -s -o /dev/null -w "%{http_code}" https://localhost:${NGINX_PORT}/nginx-health | grep -q 200 \
  && echo "[✔] Endpoint de santé NGINX opérationnel" \
  || echo "[!] Erreur : endpoint de santé NGINX non accessible"

echo "[3] Test endpoint statut NGINX"
curl -s http://localhost:${LISTEN_PORT}/nginx_status | grep -q "Active connections" \
  && echo "[✔] Endpoint de statut NGINX opérationnel" \
  || echo "[!] Erreur : endpoint de statut NGINX non accessible"

echo "[4] Test accès backend via proxy"
API_ENDPOINT="/actuator/health"
echo "    Test sur l'endpoint: ${API_ENDPOINT}"
curl -k -s -o /dev/null -w "%{http_code}\n" "https://localhost:${NGINX_PORT}${API_ENDPOINT}" | grep -q 200 \
  && echo "[✔] Proxy fonctionnel : accès backend OK" \
  || {
    echo "[!] Erreur : backend inaccessible via proxy"
    echo "    Dernières erreurs NGINX:"
    tail -n 20 /var/log/nginx/proxy-error.log
    echo "    Tentative de curl détaillée:"
    curl -k -v "https://localhost:${NGINX_PORT}${API_ENDPOINT}"
  }

echo "[+] Résumé de configuration"
echo "---------------------------------------------"
echo " Backend:   http://${BACKEND_IP}:${BACKEND_PORT}"
echo " Proxy HTTPS: https://$(hostname -I | awk '{print $1}'):${NGINX_PORT}"
echo " Monitoring: http://$(hostname -I | awk '{print $1}'):${LISTEN_PORT}/nginx_status"
echo " SELinux:    $(getenforce)"
echo "---------------------------------------------"
echo "[✔] Configuration NGINX terminée avec succès"