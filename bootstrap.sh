#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────
# CourselyLabs — bootstrap para máquina Ubuntu nueva
# ──────────────────────────────────────────────────────────────
# Lo que hace:
#   1. Instala Docker + Compose si no están.
#   2. Crea .env desde .env.example si no existe, generando secretos
#      seguros automáticamente (JWT, ENCRYPTION_SECRET, ENCRYPTION_SALT).
#   3. Levanta toda la stack en background con docker compose.
#   4. Espera a que los healthchecks pasen.
#
# Uso:
#   chmod +x bootstrap.sh
#   ./bootstrap.sh
#
# Es idempotente: si lo ejecutas varias veces, no rompe nada.
# ──────────────────────────────────────────────────────────────
set -euo pipefail

# ── Helpers ───────────────────────────────────────────────────
green()  { printf '\033[1;32m%s\033[0m\n' "$*"; }
yellow() { printf '\033[1;33m%s\033[0m\n' "$*"; }
red()    { printf '\033[1;31m%s\033[0m\n' "$*" >&2; }

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_DIR"

# ── 1. Docker ─────────────────────────────────────────────────
if ! command -v docker >/dev/null 2>&1; then
    yellow "Docker no está instalado. Instalando…"
    sudo apt-get update -qq
    sudo apt-get install -y -qq ca-certificates curl gnupg
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
        | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
        https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
        | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update -qq
    sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io \
        docker-buildx-plugin docker-compose-plugin
    sudo usermod -aG docker "$USER"
    yellow "Te han añadido al grupo docker — quizá tengas que cerrar sesión y volver."
fi

if ! docker compose version >/dev/null 2>&1; then
    red "docker compose v2 no está disponible. Aborto."
    exit 1
fi

green "✔ Docker listo"

# ── 2. .env ───────────────────────────────────────────────────
if [[ ! -f .env ]]; then
    yellow "Creando .env desde .env.example…"
    cp .env.example .env

    # Sustituir los marcadores ⚠ por secretos generados
    JWT=$(openssl rand -hex 64)
    ENC_SECRET=$(openssl rand -hex 32)
    ENC_SALT=$(openssl rand -hex 16)
    DB_PWD=$(openssl rand -hex 16)

    sed -i "s|JWT_SECRET=.*|JWT_SECRET=$JWT|" .env
    sed -i "s|APP_ENCRYPTION_SECRET=.*|APP_ENCRYPTION_SECRET=$ENC_SECRET|" .env
    sed -i "s|APP_ENCRYPTION_SALT=.*|APP_ENCRYPTION_SALT=$ENC_SALT|" .env
    sed -i "s|DB_PASSWORD=changeme|DB_PASSWORD=$DB_PWD|" .env

    green "✔ .env creado con secretos aleatorios"
    yellow "  Revísalo y ajusta ECHO_BASE_URL, FRONTEND_URL, STORAGE_PUBLIC_URL"
    yellow "  según la IP/dominio del nuevo equipo."
else
    green "✔ .env existente, no se sobrescribe"
fi

# ── 3. Levantar stack ─────────────────────────────────────────
yellow "Construyendo imágenes (la primera vez tarda ~5 min)…"
docker compose -f docker-compose.prod.yml --env-file .env build

yellow "Arrancando servicios…"
docker compose -f docker-compose.prod.yml --env-file .env up -d

# ── 4. Esperar healthchecks ───────────────────────────────────
yellow "Esperando a que postgres esté listo…"
for i in {1..30}; do
    if docker compose -f docker-compose.prod.yml ps --format json postgres 2>/dev/null \
        | grep -q '"Health":"healthy"'; then
        green "✔ Postgres healthy"
        break
    fi
    sleep 2
done

yellow "Esperando a que el backend arranque (puede tardar 30-60s)…"
for i in {1..60}; do
    if docker compose -f docker-compose.prod.yml logs backend 2>/dev/null \
        | grep -q 'Started CourselyLabsApplication'; then
        green "✔ Backend arriba"
        break
    fi
    sleep 2
done

HOST_PORT=$(grep -E '^HTTP_PORT=' .env | cut -d= -f2)
HOST_PORT=${HOST_PORT:-80}

cat <<EOF

────────────────────────────────────────────────────────────
$(green '✔ CourselyLabs desplegado')
────────────────────────────────────────────────────────────

  Frontend:   http://$(hostname -I | awk '{print $1}'):${HOST_PORT}/
  MailHog UI: http://$(hostname -I | awk '{print $1}'):8025/
  MinIO UI:   http://$(hostname -I | awk '{print $1}'):9001/

Login con los seeds:
  email:    instructor@cursos.com
  password: instructor123

Logs:    docker compose -f docker-compose.prod.yml logs -f
Parar:   docker compose -f docker-compose.prod.yml down
Reset:   docker compose -f docker-compose.prod.yml down -v   (BORRA DATOS)

EOF
