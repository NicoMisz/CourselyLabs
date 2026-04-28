# Migraciones de base de datos (Flyway)

> Cómo gestionar el schema. Toda modificación de esquema va por una migración Flyway versionada.

---

## Configuración

`application.properties`:
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.jpa.hibernate.ddl-auto=validate
```

- **`ddl-auto=validate`**: Hibernate **NO** modifica el schema en arranque. Solo valida que coincide con las entidades JPA. Si no coincide, la app no arranca.
- **`baseline-on-migrate=true`**: si se conecta a una BD existente sin tabla `flyway_schema_history`, la baseline en V0.

---

## Convenciones

### Nombres

```
V<N>__descripcion_corta_en_snake_case.sql
```

- `V` mayúscula seguida de número entero monotónicamente creciente.
- Doble underscore `__` (importante).
- Descripción en snake_case sin tildes ni caracteres especiales.

Ejemplos válidos:
- `V11__lesson_blocks.sql`
- `V12__add_email_index.sql`
- `V13__create_forums.sql`

### Reglas

- **Nunca editar** una migración aplicada en una BD viva (`develop`/`main`/producción). Hacerlo invalida el checksum y Flyway falla en arranque.
- **Siempre incluir un comentario** al inicio explicando qué hace.
- **Idempotencia donde sea posible**: `CREATE TABLE IF NOT EXISTS`, `INSERT ... ON CONFLICT DO NOTHING`. No siempre aplica.
- **Migrar datos en la misma migración** que el cambio de schema. Si V12 añade una columna `not null`, primero la añade nullable, rellena con UPDATE, y luego cambia a `not null`.
- **Usar tipos PostgreSQL nativos**: `UUID`, `TIMESTAMP`, `TEXT`, `JSONB`, etc.
- **Foreign keys**: con nombre explícito si se va a referenciar después (`CONSTRAINT fk_x_y`).

---

## Workflow para añadir una migración

### 1. Decide el número de versión

Mira la última migración existente y suma 1:

```bash
ls CourselyLabs-back/src/main/resources/db/migration/ | sort -V | tail -3
# → V9__create_lesson_resources.sql
# → V10__create_assessments.sql
# → V11__lesson_blocks.sql
```

→ Tu archivo será `V12__xxx.sql`.

> ⚠️ Si trabajas en una rama y ya hay otro `V12` en `develop`, tendrás que renumerar al `V13` antes de mergear. Para evitarlo, coordina con el equipo qué número te toca.

### 2. Crea el archivo

```bash
touch CourselyLabs-back/src/main/resources/db/migration/V12__add_forum_tables.sql
```

### 3. Escribe el SQL

```sql
-- V12: Crea tablas para el sistema de foros (uno por curso, posts y replies).

CREATE TABLE forums (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(course_id)
);

CREATE TABLE forum_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    forum_id UUID NOT NULL REFERENCES forums(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_forum_posts_forum_id ON forum_posts(forum_id);
CREATE INDEX idx_forum_posts_author_id ON forum_posts(author_id);
```

### 4. Actualiza `init_db/01_schema.sql`

Este archivo es el **schema completo** que PostgreSQL ejecuta en el primer arranque del contenedor (es un atajo para no tener que correr todas las migraciones desde cero). Tienes que reflejar el cambio aquí también para mantenerlo en sincronía.

### 5. Compila y prueba

```bash
cd CourselyLabs-back
./mvnw compile

# Reset completo (BD vacía → migraciones aplicadas en orden)
docker compose down -v
docker compose up -d
./mvnw spring-boot:run
```

Si Flyway encuentra un problema:
- **Checksum mismatch**: editaste una migración aplicada. Solución: reset (`down -v`) o repair manual.
- **Validation failed**: tu entidad JPA y el schema no coinciden. Revisa anotaciones (`@Column(nullable = false)`, longitudes, tipos).
- **Migration failed**: error SQL. Lee el log, arregla el `.sql`, reset.

### 6. Documenta

- Añade una fila a `01-overview/data-model.md` §Migraciones.
- Si afecta a una feature, actualiza la doc de esa feature en `05-features/`.
- Si rompes algo legacy, anótalo en `01-overview/state.md` §Decisiones arquitectónicas vivas.

---

## Patrones útiles

### Añadir columna nullable, rellenar, cambiar a not null

```sql
-- Paso 1: añadir nullable
ALTER TABLE courses ADD COLUMN storage_bytes BIGINT;

-- Paso 2: rellenar valores existentes
UPDATE courses SET storage_bytes = 0 WHERE storage_bytes IS NULL;

-- Paso 3: ahora sí, hacer not null
ALTER TABLE courses ALTER COLUMN storage_bytes SET NOT NULL;
ALTER TABLE courses ALTER COLUMN storage_bytes SET DEFAULT 0;
```

### Renombrar tabla preservando datos

```sql
ALTER TABLE old_name RENAME TO new_name;
-- Renombrar también las constraints, índices y secuencias asociadas si las hay
```

### Añadir índice no único

```sql
CREATE INDEX idx_lessons_section_id ON lessons(section_id);
```

### Hacer una columna nullable que era not null

```sql
ALTER TABLE lessons ALTER COLUMN type DROP NOT NULL;
```

### Migrar datos con `DO $$ ... $$`

Para flujos complejos con variables locales:

```sql
DO $$
DECLARE
    v_lesson RECORD;
    v_block_id UUID;
BEGIN
    FOR v_lesson IN SELECT * FROM lessons WHERE type IS NOT NULL
    LOOP
        v_block_id := gen_random_uuid();
        INSERT INTO lesson_blocks (id, lesson_id, type, position, text_content)
        VALUES (v_block_id, v_lesson.id, v_lesson.type, 0, v_lesson.content_text);
    END LOOP;
END $$;
```

### Eliminar constraint y volver a crearla

```sql
ALTER TABLE assessments DROP CONSTRAINT assessments_lesson_id_unique;
ALTER TABLE assessments ADD CONSTRAINT assessments_block_id_unique UNIQUE (block_id);
```

---

## Migraciones aplicadas

Histórico actualizado vía `01-overview/data-model.md` §Migraciones. Hoy:

V1 → V11. Próxima libre: **V12**.

---

## Repair (cuando algo va mal en local)

Si tras editar una migración la BD local está rota:

```bash
docker compose down -v && docker compose up -d
./mvnw spring-boot:run
```

Esto borra el volumen de PostgreSQL → ejecuta `init_db/01_schema.sql` en el primer arranque → Flyway aplica todas las migraciones encima → app arranca limpia.

> ⚠️ **NUNCA** hagas esto en producción. Para repair en producción usa `./mvnw flyway:repair` o equivalente, sin perder datos.
