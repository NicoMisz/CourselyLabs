# Variables de entorno

> Inventario de todas las variables que se pueden configurar en el proyecto. Todas tienen defaults razonables para desarrollo local.

---

## Backend (Spring Boot)

Definidas en `CourselyLabs-back/src/main/resources/application.properties` con la sintaxis `${VAR:default}`. Se pueden sobrescribir con variables de entorno o un archivo `.env` en la raíz del backend.

### Base de datos

| Variable | Default (dev) | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:4321/cursos_db` | URL JDBC |
| `DB_USER` | `myuser` | Usuario PostgreSQL |
| `DB_PASSWORD` | `secret` | Contraseña |

### Servidor

| Variable | Default | Descripción |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto HTTP del backend |
| `SHOW_SQL` | `false` | Mostrar las queries de Hibernate en logs |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:9000` | Orígenes permitidos por CORS |

### JWT

| Variable | Default | Descripción |
|---|---|---|
| `JWT_SECRET` | (clave hardcoded de dev) | **CAMBIAR EN PRODUCCIÓN.** Mínimo 256 bits. |
| `JWT_ACCESS_EXPIRATION` | `900000` | Duración del access token en ms (15 min) |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Duración del refresh token en ms (7 días) |

### Email (MailHog en dev)

| Variable | Default | Descripción |
|---|---|---|
| `MAIL_HOST` | `localhost` | SMTP host |
| `MAIL_PORT` | `1025` | SMTP puerto |
| `MAIL_USERNAME` | (vacío) | |
| `MAIL_PASSWORD` | (vacío) | |
| `MAIL_SMTP_AUTH` | `false` | |
| `MAIL_SMTP_STARTTLS` | `false` | |
| `MAIL_FROM` | `noreply@courselylabs.com` | Sender |
| `FRONTEND_URL` | `http://localhost:5173` | Para enlaces en correos (verificación, etc.) |

### Storage (MinIO)

| Variable | Default | Descripción |
|---|---|---|
| `STORAGE_ENDPOINT` | `http://localhost:9000` | URL interna del API S3 |
| `STORAGE_ACCESS_KEY` | `minioadmin` | |
| `STORAGE_SECRET_KEY` | `minioadmin` | |
| `STORAGE_BUCKET` | `courselylabs` | Nombre del bucket |
| `STORAGE_PUBLIC_URL` | `http://localhost:9000` | URL pública para signed URLs |
| `STORAGE_PRESIGN_EXPIRY` | `15` | Minutos que dura una signed URL |

### Stripe

Necesarias **solo si quieres probar el flujo Premium**.

| Variable | Default | Descripción |
|---|---|---|
| `STRIPE_SECRET_KEY` | (vacío) | `sk_test_...` |
| `STRIPE_PUBLIC_KEY` | (vacío) | `pk_test_...` |
| `STRIPE_WEBHOOK_SECRET` | (vacío) | `whsec_...` (usa `stripe listen` para obtener el local) |
| `STRIPE_MONTHLY_PRICE_ID` | (vacío) | `price_...` del producto mensual |
| `STRIPE_ANNUAL_PRICE_ID` | (vacío) | `price_...` del producto anual |

Detalles del setup Stripe en [`05-features/premium-stripe.md`](../05-features/premium-stripe.md).

---

## Frontend (Vue + Vite)

Definidas en archivos `.env*` de `CourselyLabs-front/`. Vite expone al cliente solo las que tienen prefijo `VITE_`.

| Archivo | Propósito | ¿Commitear? |
|---|---|---|
| `.env` | Defaults para todos los entornos | ✅ |
| `.env.production` | Overrides para `npm run build` | ✅ |
| `.env.local` | Overrides locales personales | ❌ (gitignored) |

### Variables disponibles

| Variable | Default (dev) | Descripción |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | URL del backend |
| `VITE_STRIPE_PUBLIC_KEY` | (vacío) | Clave pública de Stripe (la lee `loadStripe`) |

### Uso en código

```ts
const url = `${import.meta.env.VITE_API_BASE_URL}/api/courses`
```

### Añadir una variable nueva

1. Añade `VITE_TU_VAR=valor` a `.env`.
2. Añade el override en `.env.production` si aplica.
3. Úsala en código con `import.meta.env.VITE_TU_VAR`.
4. **No** la pongas en `.env.local` salvo que sea personal — esa no se sube a git.

---

## Producción (notas)

Cuando se despliegue a producción (no implementado todavía):

- `JWT_SECRET` debe generarse aleatoriamente y guardarse en un secret manager.
- `DB_PASSWORD` y `STORAGE_*` keys nunca deben estar en imágenes Docker — usar variables runtime o secrets.
- `CORS_ALLOWED_ORIGINS` debe restringirse al dominio real.
- `SHOW_SQL=false` siempre.
- `FRONTEND_URL` debe apuntar al dominio público.
- `MAIL_HOST` debe apuntar al SMTP real (SendGrid, AWS SES, etc.). MailHog **solo** funciona en desarrollo.
- Considera usar variables Spring profile (`SPRING_PROFILES_ACTIVE=prod`) para separar configs.
