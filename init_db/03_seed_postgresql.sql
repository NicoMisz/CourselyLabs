-- ============================================
-- 03: SEED — Secciones y lecciones del curso PostgreSQL
-- ============================================

DO $$
DECLARE
  v_course UUID;
  s1 UUID; s2 UUID; s3 UUID; s4 UUID;
BEGIN

SELECT id INTO v_course FROM courses WHERE slug = 'introduccion-postgresql';
IF v_course IS NULL THEN RAISE NOTICE 'Curso introduccion-postgresql no encontrado, saltando seed'; RETURN; END IF;

-- Seccion 1: Introduccion
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), v_course, 'Introduccion a PostgreSQL', 'Conceptos basicos y configuracion del entorno.', 0)
RETURNING id INTO s1;

INSERT INTO lessons (section_id, title, description, type, content_text, duration, position, is_free) VALUES
(s1, 'Que es PostgreSQL?', 'Historia y caracteristicas principales.', 'text',
'# Que es PostgreSQL?

PostgreSQL es un sistema de gestion de bases de datos relacional de codigo abierto. Es conocido por su robustez, extensibilidad y cumplimiento de estandares SQL.

## Caracteristicas principales

- **ACID compliant**: Garantiza la integridad de los datos
- **Extensible**: Puedes crear tus propios tipos de datos y funciones
- **JSON nativo**: Soporte para datos semi-estructurados
- **Full-text search**: Busqueda de texto integrada
- **Replicacion**: Soporte para alta disponibilidad', 180, 0, true),

(s1, 'Instalacion y configuracion', 'Como instalar PostgreSQL.', 'text',
'# Instalacion de PostgreSQL

## Linux (Ubuntu/Debian)
```bash
sudo apt update && sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

## macOS (Homebrew)
```bash
brew install postgresql@16
brew services start postgresql@16
```

## Verificar la instalacion
```bash
psql --version
```', 300, 1, true),

(s1, 'psql: la terminal interactiva', 'Comandos basicos de psql.', 'text',
'# psql: Terminal interactiva

## Conexion
```bash
psql -U usuario -d base_de_datos -h localhost -p 5432
```

## Comandos meta

| Comando | Descripcion |
|---------|-------------|
| `\l` | Listar bases de datos |
| `\dt` | Listar tablas |
| `\d tabla` | Describir tabla |
| `\q` | Salir |', 240, 2, false);

-- Seccion 2: SQL Basico
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), v_course, 'SQL Basico', 'Operaciones CRUD fundamentales.', 1)
RETURNING id INTO s2;

INSERT INTO lessons (section_id, title, description, type, content_text, duration, position, is_free) VALUES
(s2, 'CREATE TABLE y tipos de datos', 'Definicion de tablas.', 'text',
'# CREATE TABLE

```sql
CREATE TABLE productos (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    precio      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);
```

## Tipos de datos mas usados
INTEGER, BIGINT, SERIAL, UUID, VARCHAR(n), TEXT, NUMERIC(p,s), BOOLEAN, TIMESTAMP, JSONB', 360, 0, false),

(s2, 'INSERT, SELECT, UPDATE, DELETE', 'Las cuatro operaciones fundamentales.', 'text',
'# Operaciones CRUD

## INSERT
```sql
INSERT INTO productos (nombre, precio) VALUES (''Teclado'', 89.99);
```

## SELECT
```sql
SELECT nombre, precio FROM productos WHERE precio > 50 ORDER BY precio DESC;
```

## UPDATE
```sql
UPDATE productos SET precio = 94.99 WHERE id = 1;
```

## DELETE
```sql
DELETE FROM productos WHERE activo = FALSE;
```', 420, 1, false),

(s2, 'WHERE, ORDER BY y agregacion', 'Filtrado y funciones de agregacion.', 'text',
'# Filtrado y agregacion

```sql
SELECT COUNT(*), AVG(precio), MIN(precio), MAX(precio) FROM productos WHERE activo = TRUE;
```

## GROUP BY + HAVING
```sql
SELECT categoria, COUNT(*) AS total, AVG(precio) AS precio_medio
FROM productos GROUP BY categoria HAVING COUNT(*) > 5;
```', 360, 2, false);

-- Seccion 3: JOINs
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), v_course, 'Relaciones y JOINs', 'Como relacionar tablas.', 2)
RETURNING id INTO s3;

INSERT INTO lessons (section_id, title, description, type, content_text, duration, position, is_free) VALUES
(s3, 'Claves primarias y foraneas', 'Integridad referencial.', 'text',
'# Claves primarias y foraneas

```sql
CREATE TABLE categorias (id SERIAL PRIMARY KEY, nombre VARCHAR(50) NOT NULL UNIQUE);

CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    categoria_id INTEGER REFERENCES categorias(id) ON DELETE CASCADE
);
```', 300, 0, false),

(s3, 'INNER JOIN, LEFT JOIN, RIGHT JOIN', 'Tipos de JOIN.', 'text',
'# JOINs

## INNER JOIN
```sql
SELECT p.nombre, c.nombre AS categoria FROM productos p INNER JOIN categorias c ON p.categoria_id = c.id;
```

## LEFT JOIN
```sql
SELECT p.nombre, c.nombre AS categoria FROM productos p LEFT JOIN categorias c ON p.categoria_id = c.id;
```', 420, 1, false);

-- Seccion 4: Avanzados
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), v_course, 'Temas avanzados', 'Indices, transacciones y optimizacion.', 3)
RETURNING id INTO s4;

INSERT INTO lessons (section_id, title, description, type, content_text, duration, position, is_free) VALUES
(s4, 'Indices y rendimiento', 'Creacion de indices y EXPLAIN ANALYZE.', 'text',
'# Indices en PostgreSQL

```sql
CREATE INDEX idx_productos_nombre ON productos(nombre);
CREATE INDEX idx_productos_activos ON productos(precio) WHERE activo = TRUE;
```

## EXPLAIN ANALYZE
```sql
EXPLAIN ANALYZE SELECT * FROM productos WHERE nombre = ''Teclado'';
```', 480, 0, false),

(s4, 'Transacciones y ACID', 'BEGIN, COMMIT y ROLLBACK.', 'text',
'# Transacciones

```sql
BEGIN;
UPDATE cuentas SET saldo = saldo - 100 WHERE id = 1;
UPDATE cuentas SET saldo = saldo + 100 WHERE id = 2;
COMMIT;
```

## SAVEPOINT
```sql
BEGIN;
INSERT INTO pedidos (cliente_id, total) VALUES (1, 150);
SAVEPOINT sp1;
-- si falla algo:
ROLLBACK TO sp1;
COMMIT;
```', 360, 1, false);

END;
$$;
