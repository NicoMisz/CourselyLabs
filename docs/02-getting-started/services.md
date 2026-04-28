# Servicios de CourselyLabs

Referencia rapida de todos los servicios locales, sus puertos y como acceder.

---

## Arrancar todo

```bash
docker compose up -d         # Postgres, MailHog, MinIO
cd CourselyLabs-back
./mvnw spring-boot:run       # Backend Spring Boot
# En otra terminal:
cd CourselyLabs-front
npm run dev                  # Frontend Vite
# Para webhooks Stripe (opcional, solo si los necesitas):
stripe listen --forward-to localhost:8080/api/payments/webhook
```

---

## Puertos y URLs

| Servicio | URL | Credenciales | Notas |
|---|---|---|---|
| **Frontend (Vite dev)** | http://localhost:5173 | — | App Vue |
| **Backend (Spring Boot)** | http://localhost:8080 | — | API REST |
| **PostgreSQL** | localhost:4321 | `myuser` / `secret` | DB `cursos_db` |
| **MailHog SMTP** | localhost:1025 | — | Recibe emails salientes del backend |
| **MailHog Web UI** | http://localhost:8025 | — | Ver emails capturados |
| **MinIO API (S3)** | http://localhost:9000 | `minioadmin` / `minioadmin` | Endpoint S3 para uploads |
| **MinIO Console** | http://localhost:9001 | `minioadmin` / `minioadmin` | UI para gestionar buckets/archivos |

---

## Integraciones externas (en modo test)

| Servicio | Config | Notas |
|---|---|---|
| **Stripe** | `.env.local` del backend | Claves `sk_test_*`, `pk_test_*`, `whsec_*` + Price IDs mensual/anual |
| **Stripe CLI** | `stripe listen --forward-to localhost:8080/api/payments/webhook` | Necesario solo para que los webhooks lleguen localmente |

---

## DB

- **Puerto externo**: `4321` (mapea al 5432 interno del container)
- **Connect desde host**:
  ```bash
  docker exec -it courselylabs-db psql -U myuser -d cursos_db
  ```
- **Reset completo** (borra TODOS los datos):
  ```bash
  docker compose down -v && docker compose up -d
  ```
  El contenido de `init_db/` (schema + seeds) se ejecuta automaticamente al recrear el volumen.

---

## MinIO

- **Bucket por defecto**: `courselylabs` (se crea automaticamente al arrancar el backend)
- Almacena:
  - `thumbnails/<courseId>/...` — imagenes de portada de cursos
  - `lesson-content/<courseId>/<lessonId>/...` — videos y PDFs de lecciones
  - `resources/<courseId>/<lessonId>/...` — recursos descargables
- **Descargas**: el backend genera URLs presignadas que expiran en 15 minutos

---

## Usuarios seed (password: `admin123`)

| Email | Rol | Notas |
|---|---|---|
| `admin@cursos.com` | admin | Acceso total + panel `/admin` |
| `instructor@cursos.com` | user | Instructor de varios cursos seed |
| `student@cursos.com` | user | Estudiante con enrollments |
| `sara.martin@cursos.com` | user | Instructora de UI/UX |
| `pau.roca@cursos.com` | user | Instructor de marketing |

> Si el login del seed falla, es por incompatibilidad del hash bcrypt con Spring Security. Registra un usuario manualmente o regenera los hashes.

---

## Variables de entorno del backend

Ver `CourselyLabs-back/.env.local` (ya en `.gitignore`):

```bash
# Stripe
export STRIPE_SECRET_KEY=sk_test_...
export STRIPE_PUBLIC_KEY=pk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...
export STRIPE_MONTHLY_PRICE_ID=price_...
export STRIPE_ANNUAL_PRICE_ID=price_...

# Storage (MinIO — valores por defecto ok para dev)
export STORAGE_ENDPOINT=http://localhost:9000
export STORAGE_ACCESS_KEY=minioadmin
export STORAGE_SECRET_KEY=minioadmin
export STORAGE_BUCKET=courselylabs

# Mail (MailHog — valores por defecto ok para dev)
# No hace falta tocar nada; apunta a localhost:1025
```

---

## Troubleshooting rapido

| Problema | Solucion |
|---|---|
| Backend no arranca: "missing table X" | `docker compose down -v && docker compose up -d` (recrea DB con init_db completo) |
| Puerto 8080 ocupado | `lsof -i :8080 -t \| xargs kill` |
| Puerto 5173 ocupado | `lsof -i :5173 -t \| xargs kill` |
| Login del seed no funciona | Registra un usuario nuevo desde `/register` |
| Webhooks Stripe no llegan | Verifica que `stripe listen` esta corriendo |
| MinIO bucket no existe | Se crea automaticamente al arrancar el backend |
