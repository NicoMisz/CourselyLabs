#!/usr/bin/env bash
# ──────────────────────────────────────────────
# seed.sh — Ejecuta los seeds de datos de prueba
# Uso:
#   ./seed.sh              → ejecuta todos los seeds
#   ./seed.sh flamenco     → ejecuta solo el seed de flamenco
#   ./seed.sh postgresql   → ejecuta solo el seed de postgresql
# ──────────────────────────────────────────────

set -euo pipefail

SEED_DIR="CourselyLabs-back/src/main/resources/db/seed"
CONTAINER="${DB_CONTAINER:-courselylabs-db}"
DB_NAME="${DB_NAME:-cursos_db}"
DB_USER="${DB_USER:-myuser}"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Verificar que el contenedor esta corriendo
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  echo -e "${RED}Error: El contenedor '${CONTAINER}' no esta corriendo.${NC}"
  echo "Ejecuta: docker compose up -d"
  exit 1
fi

# Verificar que el directorio de seeds existe
if [ ! -d "$SEED_DIR" ]; then
  echo -e "${RED}Error: No se encontro el directorio de seeds: ${SEED_DIR}${NC}"
  exit 1
fi

run_seed() {
  local file="$1"
  local name
  name=$(basename "$file")
  echo -ne "  ${YELLOW}Ejecutando${NC} ${name}... "
  if docker exec -i "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" < "$file" > /dev/null 2>&1; then
    echo -e "${GREEN}OK${NC}"
  else
    echo -e "${RED}ERROR${NC}"
    echo "    Detalle:"
    docker exec -i "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" < "$file" 2>&1 | head -5
    return 1
  fi
}

echo ""
echo "🌱 CourselyLabs — Seed de datos"
echo "───────────────────────────────"

if [ $# -eq 0 ]; then
  # Ejecutar todos los seeds
  count=0
  for file in "$SEED_DIR"/seed_*.sql; do
    [ -f "$file" ] || continue
    run_seed "$file"
    count=$((count + 1))
  done
  echo "───────────────────────────────"
  echo -e "${GREEN}$count seed(s) ejecutados.${NC}"
else
  # Ejecutar seed especifico
  file="$SEED_DIR/seed_${1}_course.sql"
  if [ ! -f "$file" ]; then
    echo -e "${RED}Error: No existe el seed '${1}'.${NC}"
    echo "Seeds disponibles:"
    for f in "$SEED_DIR"/seed_*.sql; do
      [ -f "$f" ] || continue
      name=$(basename "$f" .sql | sed 's/seed_/  /' | sed 's/_course//')
      echo "  $name"
    done
    exit 1
  fi
  run_seed "$file"
fi

echo ""
