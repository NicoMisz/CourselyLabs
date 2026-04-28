# Datos de prueba (seeders)

> Cómo poblar la BD con datos para desarrollo y testing manual. Para reset completo ver [`setup.md`](setup.md#reset-completo).

---

## Cuándo se ejecutan

Los archivos en `init_db/` los ejecuta **PostgreSQL automáticamente** la primera vez que se crea el contenedor (gracias al volumen `/docker-entrypoint-initdb.d`). Después solo se vuelve a ejecutar si se hace `docker compose down -v` para borrar volúmenes.

```yaml
# docker-compose.yml
volumes:
  - ./init_db:/docker-entrypoint-initdb.d:ro
```

---

## Archivos disponibles

| Archivo | Contenido |
|---|---|
| `01_schema.sql` | Schema completo (espejo de Flyway aplicado, para inicialización limpia) |
| `02_seed_base.sql` | Categorías, usuarios de prueba, cursos publicados |
| `03_seed_postgresql.sql` | Curso completo "Introducción a PostgreSQL" con secciones y lecciones |
| `04_seed_flamenco.sql` | Curso completo "Flamenco" (datos de prueba alternativos) |
| `05_seed_prerequisites.sql` | Prerequisitos entre cursos para probar el bloqueo |

> Algunos seeders tienen contenido **mezclado catalán/castellano** (legacy). El proyecto es solo castellano hoy en UI; cuando se actualicen los seeders se traducirá. No se rehace por compatibilidad con datos existentes.

---

## Usuarios de prueba (todos con password `admin123`)

| Email | Rol | Notas |
|---|---|---|
| `admin@cursos.com` | `admin` | Administrador del sistema |
| `instructor@cursos.com` | `user` | Actúa como instructor (ha creado cursos) |
| `student@cursos.com` | `user` | Estudiante con varias inscripciones |
| `sara.martin@cursos.com` | `user` | Instructora UX/UI |
| `pau.roca@cursos.com` | `user` | Instructor marketing |
| `laia.font@cursos.com` | `user` | Estudiante |
| `marc.puig@cursos.com` | `user` | Estudiante |
| `ana.vidal@cursos.com` | `user` | Sin verificar (probar flujo de verificación) |
| `jordi.mas@cursos.com` | `user` | Estudiante |
| `clara.soler@cursos.com` | `user` | Estudiante |
| `inactive@cursos.com` | `user` | Inactivo (probar bloqueo) |

> Si el login falla por incompatibilidad del hash bcrypt con la versión de Spring Security en uso, registra un usuario manualmente desde `/register` o pide a un admin que te conceda Premium desde el panel admin.

---

## Reset completo + recarga de seeders

```bash
docker compose down -v       # borra el volume de PostgreSQL
docker compose up -d         # arranca; PostgreSQL re-ejecuta init_db/*
cd CourselyLabs-back
./mvnw spring-boot:run       # Flyway aplica todas las migraciones encima del schema
```

Tras esto tendrás:
- Schema completo aplicado.
- Seeders cargados.
- Usuarios listos para probar.

---

## Carga selectiva de seeders (BD existente)

Si ya tienes el contenedor corriendo y solo quieres recargar un seed concreto sin perder datos:

```bash
# Conectarse al contenedor
docker exec -i courselylabs-db psql -U myuser -d cursos_db < init_db/03_seed_postgresql.sql
```

> ⚠️ Esto puede fallar si los datos ya existen (claves duplicadas). Algunos seeders tienen guard `IF NOT EXISTS` o `ON CONFLICT DO NOTHING`, otros no. Lee el seed antes.

---

## Crear seeders nuevos

Pautas:

1. Nombrar `0N_seed_xxx.sql` con un número que indique orden de ejecución.
2. Usar `gen_random_uuid()` para PKs UUID.
3. Usar `DO $$ ... $$` con variables locales para flujos complejos (ver `03_seed_postgresql.sql`).
4. Hacer los seeders **idempotentes** cuando sea posible (`ON CONFLICT DO NOTHING`).
5. Acompañar con un comentario al inicio explicando qué crea.

Antes de commitear, prueba:
```bash
docker compose down -v
docker compose up -d
# Verifica logs del contenedor
docker compose logs postgres | grep -i error
```

---

## Conceder Premium manualmente

Si necesitas un usuario Premium para probar features sin pasar por Stripe:

1. Loguéate como `admin@cursos.com`.
2. Ve a `/admin/usuarios`.
3. Busca el usuario y pulsa "Conceder Premium".

Esto crea una suscripción activa "manual" en la tabla `subscriptions` sin pasar por Stripe.
