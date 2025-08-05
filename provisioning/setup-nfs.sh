#!/bin/bash
set -e
export PATH=$PATH:/usr/share/elasticsearch/bin

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

# mise à jour des dépôts et du système
echo "[+] Configuration des dépôts et mise à jour du système"
dnf install -y epel-release
dnf install -y dnf-plugins-core
dnf config-manager --set-enabled crb
dnf update -y --nobest --skip-broken kernel
sudo dnf install -y nmap-ncat