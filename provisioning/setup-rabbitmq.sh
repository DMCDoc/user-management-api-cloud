#!/bin/bash
set -e

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
required_vars=(
    "RABBITMQ_USER" 
    "RABBITMQ_PASSWORD"
    "RABBITMQ_PORT_AMQP"
    "RABBITMQ_PORT_HTTP" 
    "RABBITMQ_PORT_MQTT"
    "RABBITMQ_PORT_MQTT_SSL_TLS" 
    "RABBITMQ_PORT_STOMP"
    "RABBITMQ_PORT_STOMP_SSL_TLS")
    
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "[!] Variable $var non définie dans $ENV_FILE" >&2
        exit 1
    fi
done

echo "[+] Mise à jour des paquets"
dnf update -y

echo "[+] Activation du dépôt EPEL"
dnf install -y epel-release

echo "[+] Installation des dépendances : Erlang + RabbitMQ"
# Ajout du dépôt officiel pour des versions plus récentes
curl -s https://packagecloud.io/install/repositories/rabbitmq/rabbitmq-server/script.rpm.sh | sudo bash
dnf install -y erlang rabbitmq-server

echo "[+] Activation et démarrage de RabbitMQ"
systemctl enable --now rabbitmq-server

echo "[+] Attente que RabbitMQ démarre..."
until rabbitmqctl wait --pid 1 --timeout 30 &>/dev/null; do
    sleep 1
done

echo "[+] Activation de l’interface de gestion web"
rabbitmq-plugins enable rabbitmq_management rabbitmq_prometheus

echo "[+] Création de l’utilisateur RabbitMQ 'userapp'"
# Suppression de l'utilisateur par défaut (sécurité)
rabbitmqctl delete_user guest 2>/dev/null || true

rabbitmqctl add_user "$RABBITMQ_USER" "$RABBITMQ_PASSWORD"
rabbitmqctl set_user_tags "$RABBITMQ_USER" administrator
rabbitmqctl set_permissions -p / "$RABBITMQ_USER" ".*" ".*" ".*"

echo "[+] Configuration du throttling (optimisation locale)"
rabbitmqctl set_vm_memory_high_watermark 0.8
rabbitmqctl set_disk_free_limit 1GB

echo "[+] Ouverture des ports dans le pare-feu"
ports=(5672 15672 1883 8883 61613 61614)
if command -v firewall-cmd &>/dev/null; then
    systemctl enable --now firewalld
    echo "[+] Firewalld activé"
    for port in "${ports[@]}"; do
        firewall-cmd --add-port=${port}/tcp --permanent
    done
    firewall-cmd --reload
fi
echo "[+] Ports ouverts : ${ports[*]}"




echo "[+] Redémarrage du service pour prise en compte"
systemctl restart rabbitmq-server


# Vérifications améliorées
echo "[+] Vérification de l'état"
rabbitmqctl status
echo ""

echo "[+] RabbitMQ écoute sur les ports suivants :"
ss -tulpn | grep -E '5672|15672|1883|8883|61613|61614'

echo "[✔] RabbitMQ est prêt sur :"
echo "     - AMQP              : 5672"
echo "     - UI Web            : http://$(hostname -I | awk '{print $1}'):15672"
echo "     - Utilisateur admin : userapp/userappmp"

# Vérification de la création de l'utilisateur
if rabbitmqctl list_users | grep -q userapp; then
    echo "[✓] Utilisateur 'userapp' bien créé"
else
    echo "[!] Échec de la création de l'utilisateur" >&2
    exit 1
fi


# Verification du dossier de données
if [ ! -d /var/lib/rabbitmq/mnesia ]; then
    echo "[!] Le dossier de données RabbitMQ n'existe pas !" >&2
    exit 1
fi
echo "[+] Dossier de données : /var/lib/rabbitmq/mnesia"

# Vérification de la santé du service RabbitMQ
if rabbitmqctl node_health_check; then
    echo "[✓] Santé du service vérifiée"
else
    echo "[!] Problème de santé détecté" >&2
fi