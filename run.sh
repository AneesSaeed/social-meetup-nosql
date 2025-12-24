#!/bin/bash
set -e

echo "Meetup 5DON4D Project - Installation"
echo "=========================================="

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info() { echo -e "${GREEN}[INFO]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Vérification des prérequis
command -v docker >/dev/null 2>&1 || { error "Docker n'est pas installé."; exit 1; }
command -v mvn >/dev/null 2>&1 || { error "Maven n'est pas installé."; exit 1; }
command -v ng >/dev/null 2>&1 || { error "Angular CLI n'est pas installé."; exit 1; }

# Lancement du backend
cd backend || { error "Dossier backend introuvable."; exit 1; }

info "Lancement de Docker..."
docker compose up -d
#docker compose start
info "Docker lancé !"

info "Lancement du backend Spring..."
mvn clean spring-boot:run &
info "Spring lancé !"

# Lancement du frontend
cd ../frontend || { error "Dossier frontend introuvable."; exit 1; }

info "Lancement du frontend Angular..."
ng serve &
info "Angular lancé !"

info "Tout est lancé !"
wait
