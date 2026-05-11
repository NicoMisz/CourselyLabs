# Guía de despliegue — CourselyLabs

Todo el stack en Docker: BD, mail, storage, backend y frontend. Para una máquina Ubuntu nueva, **basta un script**.

## Despliegue rápido (Ubuntu 22.04+)

```bash
# 1. Clonar el repo
git clone <url-del-repo> /opt/courselylabs
cd /opt/courselylabs

# 2. Ejecutar bootstrap (instala Docker si no está + arranca todo)
chmod +x bootstrap.sh
./bootstrap.sh
```

Eso es todo. El script:

1. Instala Docker + Compose si no están.
2. Crea `.env` desde `.env.example` con secretos aleatorios (`JWT_SECRET`, claves de cifrado, password de BD).
3. Construye las imágenes (5 min la primera vez).
4. Arranca toda la stack en background.

Cuando termina, te imprime las URLs:

- **Frontend** en `http://<IP-del-server>/`
- **MailHog UI** (ver emails capturados) en `http://<IP-del-server>:8025/`
- **MinIO console** en `http://<IP-del-server>:9001/`

Login con los seeds:
- email: `instructor@cursos.com`
- password: `instructor123`

## Antes de la demo en LAN — ajustar `.env`

Después del primer `./bootstrap.sh` revisa y ajusta `.env`:

```bash
nano .env
```

Variables a revisar según tu entorno LAN:

```env
# Pon la IP real del server (no localhost) — la verán los browsers de los alumnos
FRONTEND_URL=http://192.168.1.50
STORAGE_PUBLIC_URL=http://192.168.1.50:9000
CORS_ALLOWED_ORIGINS=http://192.168.1.50

# Dirección a la que el backend llamará echo
ECHO_BASE_URL=https://echo.lab
# o si echo.lab no resuelve desde este equipo:
# ECHO_BASE_URL=https://192.168.248.X
```

Después de cambiar `.env`, reinicia los servicios:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

## Comandos habituales

```bash
# Ver logs en vivo
docker compose -f docker-compose.prod.yml logs -f

# Logs solo del backend
docker compose -f docker-compose.prod.yml logs -f backend

# Reiniciar un servicio (ej. tras editar .env)
docker compose -f docker-compose.prod.yml restart backend

# Parar todo (conserva datos)
docker compose -f docker-compose.prod.yml down

# Parar y BORRAR datos (postgres + minio)
docker compose -f docker-compose.prod.yml down -v

# Actualizar tras un git pull
git pull
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

## Backup de datos

Los datos viven en dos volúmenes Docker. Backup manual:

```bash
# Postgres
docker exec courselylabs-db pg_dump -U myuser cursos_db | gzip > backup-$(date +%F).sql.gz

# MinIO (vídeos, PDFs, recursos)
docker run --rm -v courselylabs_minio_data:/data -v "$PWD":/backup alpine \
    tar -czf /backup/minio-$(date +%F).tar.gz -C / data
```

Restaurar:

```bash
# Postgres
gunzip < backup-XXXX.sql.gz | docker exec -i courselylabs-db psql -U myuser cursos_db

# MinIO
docker run --rm -v courselylabs_minio_data:/data -v "$PWD":/backup alpine \
    sh -c "cd / && tar -xzf /backup/minio-XXXX.tar.gz"
```

## Topología en LAN

```
        ┌────────────────────────────────────────┐
        │  Servidor CourselyLabs (Ubuntu, Docker)│
        │  ┌─────────────┐    ┌──────────────┐   │
Alumno ─┼─→│ nginx :80   │──→│ backend :8080 │   │
   ▲    │  │ (frontend)  │    │ (Spring)     │   │
   │    │  └─────────────┘    └─────┬────────┘   │
   │    │                            │            │
   │    │     ┌─────────────┐  ┌─────▼───────┐   │
   │    │     │ postgres    │  │ minio :9000 │←──┼─ Alumno (descarga
   │    │     │ (interno)   │  │ (público)   │   │   archivos firmados)
   │    │     └─────────────┘  └─────────────┘   │
   │    │     ┌─────────────┐                    │
   │    │     │ mailhog     │                    │
   │    │     │ (interno)   │                    │
   │    │     └─────────────┘                    │
   │    └────────────────────┬───────────────────┘
   │                         │
   │                         │ Backend llama
   │                         ▼ HTTPS a echo
   │              ┌──────────────────┐
   └──────────────┤  Server echo.lab │ (otro equipo de la LAN)
   (browser carga │  api + viewer    │
    iframe noVNC) └──────────────────┘
```

## De LAN a internet (cuando llegue el momento)

Cambios principales:

1. **DNS público** para `courselylabs.tudominio.com` y `echo.tudominio.com`.
2. **Let's Encrypt**: añadir `certbot` y configurar nginx para HTTPS. Cambiar el `nginx.conf` del frontend a `listen 443 ssl`.
3. `ECHO_TRUST_ALL_CERTS=false` en `.env`.
4. **CORS y CSP restringidos**: cambiar `*` por el dominio real.
5. **Firewall**: solo 80, 443 y 22 (SSH) abiertos.
6. **Stripe live keys** si vas a procesar pagos reales.

## Solución de problemas

**El backend no arranca**: mira los logs.
```bash
docker compose -f docker-compose.prod.yml logs backend | tail -50
```

Errores típicos:
- `Schema validation: missing table X` → la BD tiene schema viejo. `down -v` y volver a levantar (BORRA DATOS) o aplicar la migración Flyway manualmente.
- `JWT_SECRET es obligatorio` → falta una var en `.env`.

**El frontend muestra "No se puede conectar"**: el backend no está arriba o nginx no llega a él.
```bash
docker compose -f docker-compose.prod.yml ps   # los 5 servicios deben estar Up (healthy)
```

**Los emails de verificación no llegan**: en LAN usamos MailHog que **captura todos los emails**. Míralos en `http://<IP-server>:8025/`. Para enviar de verdad, sustituye en `.env` las variables `MAIL_HOST`/`MAIL_PORT`/credenciales por las de un SMTP real.

**Los archivos subidos no se descargan**: revisa `STORAGE_PUBLIC_URL` en `.env`. Debe ser una URL que el browser del alumno pueda alcanzar (no `localhost` si está en otro equipo).

**El iframe del laboratorio falla**: revisa primero que `https://echo.lab` (o la URL que pusiste) responde desde el server. Mira la documentación de la integración con echo en [docs/05-features/](docs/05-features/) (cuando exista) o el `CHANGELOG.md` sección `feature/echolab-labs`.
