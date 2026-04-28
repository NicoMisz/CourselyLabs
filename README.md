# CourselyLabs

Plataforma de cursos online en castellano con autoría asistida, evaluaciones variadas y suscripción Premium.

> **Stack**: Vue 3 + Quasar + Vite (frontend) · Spring Boot 4 + Java 21 + PostgreSQL 16 (backend) · MinIO + MailHog + Stripe.

---

## Quick start (5 comandos)

Requisitos: Java 21, Node 20.19+, Docker.

```bash
git clone <url>
cd CourselyLabs
docker compose up -d                       # postgres + mailhog + minio
(cd CourselyLabs-back  && ./mvnw spring-boot:run)   # → :8080
(cd CourselyLabs-front && npm install && npm run dev)  # → :5173
```

Login con un seeder: `admin@cursos.com` / `admin123` (ver [`docs/02-getting-started/seeders.md`](docs/02-getting-started/seeders.md) para más usuarios).

Detalles, env vars y troubleshooting: [`docs/02-getting-started/setup.md`](docs/02-getting-started/setup.md).

---

## Documentación

Toda la documentación técnica vive en [`docs/`](docs/). Punto de entrada: [`docs/README.md`](docs/README.md).

**Atajos según para qué vienes**:

- 🆕 **Llegas por primera vez** → [`docs/01-overview/state.md`](docs/01-overview/state.md) (snapshot completo en 10 min).
- 🚀 **Quieres levantarlo** → [`docs/02-getting-started/setup.md`](docs/02-getting-started/setup.md).
- 🎨 **Vas a tocar el frontend** → [`docs/03-frontend/`](docs/03-frontend/).
- ⚙️ **Vas a tocar el backend** → [`docs/04-backend/`](docs/04-backend/).
- 🧱 **Quieres entender una feature concreta** → [`docs/05-features/`](docs/05-features/).
- 🛠️ **Quieres añadir una migración Flyway** → [`docs/04-backend/migrations.md`](docs/04-backend/migrations.md).
- ✏️ **Quieres añadir strings de UI** → [`docs/03-frontend/i18n.md`](docs/03-frontend/i18n.md).

---

## Estructura del repositorio

```
CourselyLabs/
├── CourselyLabs-back/          # Spring Boot
├── CourselyLabs-front/         # Vue 3 + Quasar
├── init_db/                    # Schema + seeders SQL
├── docker-compose.yml          # Postgres + MailHog + MinIO
├── docs/                       # Toda la documentación técnica
├── CLAUDE.md                   # Instrucciones para Claude (asistente)
├── CHANGELOG.md                # Registro de cambios global
└── README.md                   # Este archivo
```

---

## Contribuir

1. Crea una rama desde `develop` siguiendo la convención (`feature/...`, `fix/...`, `chore/...`).
2. Haz tus cambios respetando [`docs/07-conventions/code-style.md`](docs/07-conventions/code-style.md).
3. Verifica que el build pasa:
   ```bash
   (cd CourselyLabs-back && ./mvnw clean compile)
   (cd CourselyLabs-front && npm run build)
   ```
4. Actualiza [`CHANGELOG.md`](CHANGELOG.md) y los docs relevantes en [`docs/`](docs/).
5. Abre un PR siguiendo la plantilla de [`docs/07-conventions/git-workflow.md`](docs/07-conventions/git-workflow.md).

---

## Licencia y contacto

Pendiente de definir.
