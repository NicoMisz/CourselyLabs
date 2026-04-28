# Setup local

> Cómo levantar el proyecto en tu máquina. Para servicios y puertos exactos, ver [`services.md`](services.md).

---

## Requisitos previos

- **Java 21+** (el proyecto usa Java 21).
- **Node.js 20.19+** o 22.12+.
- **Docker** y **Docker Compose** (instalados y arrancados).
- **Git**.

Verifica:
```bash
java -version
node -v
docker --version && docker compose version
```

---

## 1. Clonar e ir a la raíz

```bash
git clone <url-del-repositorio>
cd CourselyLabs
```

## 2. Levantar los servicios de infraestructura

PostgreSQL, MailHog y MinIO se levantan con Docker Compose desde la raíz:

```bash
docker compose up -d
```

Esto deja corriendo:
- **PostgreSQL** en `localhost:4321` (DB `cursos_db`, usuario `myuser`, password `secret`).
- **MailHog SMTP** en `localhost:1025` y UI web en `http://localhost:8025`.
- **MinIO** API en `localhost:9000` y consola en `http://localhost:9001` (admin/admin: `minioadmin`/`minioadmin`).

> En el primer arranque, PostgreSQL ejecuta los scripts en `init_db/` (schema + seeders básicos). Para más datos de prueba, ver [`seeders.md`](seeders.md).

## 3. Backend (Spring Boot)

```bash
cd CourselyLabs-back
./mvnw spring-boot:run
```

Disponible en `http://localhost:8080`.

Variables que puedes querer poner en `.env` o exportar en la shell antes de arrancar (todas tienen defaults razonables):

- `STRIPE_SECRET_KEY`, `STRIPE_PUBLIC_KEY`, `STRIPE_WEBHOOK_SECRET`, `STRIPE_MONTHLY_PRICE_ID`, `STRIPE_ANNUAL_PRICE_ID` — necesarias si quieres probar el flujo de Premium.
- `JWT_SECRET` — se genera por defecto pero **no la uses en producción**.

Lista completa en [`env-vars.md`](env-vars.md).

## 4. Frontend (Vue 3 + Quasar)

En otra terminal:

```bash
cd CourselyLabs-front
npm install
npm run dev
```

Disponible en `http://localhost:5173` (o 5174 si el 5173 está ocupado).

---

## Comandos útiles

### Backend
```bash
./mvnw clean compile           # compilar
./mvnw spring-boot:run         # arrancar
./mvnw test                    # tests (limitados de momento)
./mvnw clean package           # construir el .jar
```

### Frontend
```bash
npm run dev                    # desarrollo con HMR
npm run build                  # build de producción a dist/
npm run preview                # servir el build
npm run type-check             # vue-tsc estricto
npm run lint                   # eslint
npm run format                 # prettier
npm run test:unit              # vitest
```

### Infra
```bash
docker compose up -d           # arrancar todo
docker compose down            # parar (preserva volumes)
docker compose down -v         # parar y borrar volumes (RESET COMPLETO)
docker compose logs -f         # logs en vivo
docker compose ps              # estado
```

---

## Reset completo

Si necesitas empezar de cero (BD vacía, MinIO vacío):

```bash
docker compose down -v
docker compose up -d
```

PostgreSQL volverá a ejecutar los scripts de `init_db/` y aplicará todas las migraciones Flyway al arrancar el backend.

---

## Solución de problemas

### "Port 8080 is already in use"
Cambia el puerto del backend:
```bash
SERVER_PORT=8081 ./mvnw spring-boot:run
```

### "Could not connect to PostgreSQL"
- Comprueba que está corriendo: `docker compose ps`.
- Reinicia: `docker compose restart postgres`.
- Verifica el puerto: el contenedor expone PostgreSQL en `4321`, no `5432`.

### "MinIO bucket does not exist"
El bucket `courselylabs` se crea en el primer arranque del backend. Si fallara, créalo manualmente desde la consola de MinIO (`http://localhost:9001`).

### Cualquier error en el front después de cambiar dependencias
```bash
rm -rf node_modules package-lock.json
npm install
```

### El correo de verificación no llega
Está llegando a MailHog: abre `http://localhost:8025` para ver la bandeja.

### "Migration checksum mismatch" en Flyway
Si has tocado un archivo de migración ya aplicado:
```bash
docker compose down -v && docker compose up -d
```
y reinicia el backend. **No edites migraciones aplicadas en producción.**
