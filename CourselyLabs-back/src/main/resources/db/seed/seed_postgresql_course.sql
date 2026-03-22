-- Seed: Secciones y lecciones para 'Introduccio a PostgreSQL'

DO $$
DECLARE
  course UUID := '9e34582e-a93e-4415-9dae-38a3c5400525';
  s1 UUID; s2 UUID; s3 UUID; s4 UUID;
BEGIN

-- Seccion 1: Introduccion
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course, 'Introduccion a PostgreSQL', 'Conceptos basicos y configuracion del entorno.', 0)
RETURNING id INTO s1;

INSERT INTO lessons (section_id, title, description, type, content_text, duration, position, is_free) VALUES
(s1, 'Que es PostgreSQL?', 'Historia y caracteristicas principales de PostgreSQL.', 'text',
'# Que es PostgreSQL?

PostgreSQL es un sistema de gestion de bases de datos relacional de codigo abierto. Es conocido por su robustez, extensibilidad y cumplimiento de estandares SQL.

## Caracteristicas principales

- **ACID compliant**: Garantiza la integridad de los datos
- **Extensible**: Puedes crear tus propios tipos de datos, funciones y operadores
- **JSON nativo**: Soporte para datos semi-estructurados
- **Full-text search**: Busqueda de texto integrada
- **Replicacion**: Soporte para alta disponibilidad

## Historia

PostgreSQL nacio en 1986 como proyecto academico en la Universidad de Berkeley (California). Originalmente se llamaba POSTGRES y fue creado por Michael Stonebraker.

```sql
-- Tu primera consulta
SELECT version();
```', 180, 0, true),

(s1, 'Instalacion y configuracion', 'Como instalar PostgreSQL en Linux, macOS y Windows.', 'text',
'# Instalacion de PostgreSQL

## Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

## macOS (Homebrew)

```bash
brew install postgresql@16
brew services start postgresql@16
```

## Verificar la instalacion

```bash
psql --version
sudo -u postgres psql -c "SELECT version();"
```

## Primer acceso

```bash
sudo -u postgres psql
\l          -- listar bases de datos
\du         -- listar usuarios
\q          -- salir
```', 300, 1, true),

(s1, 'psql: la terminal interactiva', 'Comandos basicos de la herramienta de linea de comandos.', 'text',
'# psql: Terminal interactiva

## Conexion

```bash
psql -U usuario -d base_de_datos -h localhost -p 5432
```

## Comandos meta (backslash)

| Comando | Descripcion |
|---------|-------------|
| `\l` | Listar bases de datos |
| `\c dbname` | Conectar a otra base de datos |
| `\dt` | Listar tablas |
| `\d tabla` | Describir una tabla |
| `\di` | Listar indices |
| `\df` | Listar funciones |
| `\q` | Salir |

## Ejecucion de archivos SQL

```bash
psql -U usuario -d dbname -f script.sql
```', 240, 2, false);

-- Seccion 2: SQL Basico
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course, 'SQL Basico', 'Operaciones CRUD fundamentales.', 1)
RETURNING id INTO s2;

INSERT INTO lessons (section_id, title, description, type, content_text, duration, position, is_free) VALUES
(s2, 'CREATE TABLE y tipos de datos', 'Definicion de tablas y los tipos de datos mas comunes.', 'text',
'# CREATE TABLE

## Sintaxis basica

```sql
CREATE TABLE productos (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    stock       INTEGER NOT NULL DEFAULT 0,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);
```

## Tipos de datos mas usados

| Tipo | Descripcion |
|------|-------------|
| `INTEGER` | Entero (4 bytes) |
| `BIGINT` | Entero grande (8 bytes) |
| `SERIAL` | Entero autoincremental |
| `UUID` | Identificador unico |
| `VARCHAR(n)` | Texto con limite |
| `TEXT` | Texto sin limite |
| `NUMERIC(p,s)` | Decimal exacto |
| `BOOLEAN` | Verdadero/falso |
| `TIMESTAMP` | Fecha y hora |
| `JSONB` | JSON binario |', 360, 0, false),

(s2, 'INSERT, SELECT, UPDATE, DELETE', 'Las cuatro operaciones fundamentales de SQL.', 'text',
'# Operaciones CRUD

## INSERT

```sql
INSERT INTO productos (nombre, precio, stock)
VALUES (''Teclado mecanico'', 89.99, 50);

-- Insertar multiples filas
INSERT INTO productos (nombre, precio, stock) VALUES
    (''Raton gaming'', 49.99, 100),
    (''Monitor 27"'', 299.99, 20),
    (''Auriculares'', 79.99, 75);
```

## SELECT

```sql
SELECT * FROM productos;

SELECT nombre, precio
FROM productos
WHERE precio > 50 AND activo = TRUE
ORDER BY precio DESC
LIMIT 10;
```

## UPDATE

```sql
UPDATE productos
SET precio = 94.99, stock = stock - 1
WHERE id = 1;
```

## DELETE

```sql
DELETE FROM productos WHERE activo = FALSE;
```', 420, 1, false),

(s2, 'WHERE, ORDER BY y funciones de agregacion', 'Filtrado, ordenacion y funciones como COUNT, SUM, AVG.', 'text',
'# Filtrado y agregacion

## WHERE con operadores

```sql
SELECT * FROM productos WHERE precio BETWEEN 50 AND 100;
SELECT * FROM productos WHERE nombre LIKE ''%gaming%'';
SELECT * FROM productos WHERE id IN (1, 3, 5);
SELECT * FROM productos WHERE descripcion IS NOT NULL;
```

## ORDER BY

```sql
SELECT nombre, precio
FROM productos
ORDER BY precio ASC, nombre DESC;
```

## Funciones de agregacion

```sql
SELECT
    COUNT(*) AS total_productos,
    SUM(stock) AS stock_total,
    AVG(precio) AS precio_medio,
    MIN(precio) AS mas_barato,
    MAX(precio) AS mas_caro
FROM productos
WHERE activo = TRUE;
```

## GROUP BY + HAVING

```sql
SELECT
    categoria,
    COUNT(*) AS total,
    AVG(precio) AS precio_medio
FROM productos
GROUP BY categoria
HAVING COUNT(*) > 5
ORDER BY precio_medio DESC;
```', 360, 2, false);

-- Seccion 3: Relaciones y JOINs
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course, 'Relaciones y JOINs', 'Como relacionar tablas y hacer consultas complejas.', 2)
RETURNING id INTO s3;

INSERT INTO lessons (section_id, title, description, type, content_text, duration, position, is_free) VALUES
(s3, 'Claves primarias y foraneas', 'Restricciones de integridad referencial.', 'text',
'# Claves primarias y foraneas

## Clave primaria (PRIMARY KEY)

```sql
CREATE TABLE categorias (
    id   SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);
```

## Clave foranea (FOREIGN KEY)

```sql
CREATE TABLE productos (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    categoria_id INTEGER REFERENCES categorias(id),
    precio      NUMERIC(10,2) NOT NULL
);
```

## ON DELETE / ON UPDATE

```sql
-- CASCADE: eliminar productos si se elimina la categoria
categoria_id INTEGER REFERENCES categorias(id) ON DELETE CASCADE

-- SET NULL: poner NULL si se elimina la categoria
categoria_id INTEGER REFERENCES categorias(id) ON DELETE SET NULL

-- RESTRICT (default): impedir eliminar si hay productos
categoria_id INTEGER REFERENCES categorias(id) ON DELETE RESTRICT
```', 300, 0, false),

(s3, 'INNER JOIN, LEFT JOIN, RIGHT JOIN', 'Los diferentes tipos de JOIN explicados con ejemplos.', 'text',
'# JOINs en PostgreSQL

## INNER JOIN

Devuelve solo las filas que tienen coincidencia en ambas tablas.

```sql
SELECT p.nombre, p.precio, c.nombre AS categoria
FROM productos p
INNER JOIN categorias c ON p.categoria_id = c.id;
```

## LEFT JOIN

Devuelve todas las filas de la tabla izquierda, con o sin coincidencia.

```sql
SELECT p.nombre, c.nombre AS categoria
FROM productos p
LEFT JOIN categorias c ON p.categoria_id = c.id;
```

## RIGHT JOIN

Devuelve todas las filas de la tabla derecha.

```sql
SELECT p.nombre, c.nombre AS categoria
FROM productos p
RIGHT JOIN categorias c ON p.categoria_id = c.id;
```

## Multiples JOINs

```sql
SELECT
    p.nombre AS producto,
    c.nombre AS categoria,
    pr.nombre AS proveedor
FROM productos p
JOIN categorias c ON p.categoria_id = c.id
JOIN proveedores pr ON p.proveedor_id = pr.id
WHERE p.activo = TRUE;
```', 420, 1, false);

-- Seccion 4: Temas avanzados
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course, 'Temas avanzados', 'Indices, vistas, transacciones y optimizacion.', 3)
RETURNING id INTO s4;

INSERT INTO lessons (section_id, title, description, type, content_text, duration, position, is_free) VALUES
(s4, 'Indices y rendimiento', 'Como crear indices y analizar el plan de ejecucion.', 'text',
'# Indices en PostgreSQL

## Crear un indice

```sql
CREATE INDEX idx_productos_nombre ON productos(nombre);
CREATE INDEX idx_productos_precio ON productos(precio);
CREATE UNIQUE INDEX idx_productos_sku ON productos(sku);
```

## Tipos de indice

- **B-tree** (default): Para comparaciones =, <, >, BETWEEN
- **Hash**: Solo para igualdad (=)
- **GIN**: Para arrays, JSONB, full-text search
- **GiST**: Para datos geometricos, rangos

```sql
-- Indice GIN para JSONB
CREATE INDEX idx_datos_jsonb ON productos USING GIN (metadata);

-- Indice parcial
CREATE INDEX idx_productos_activos ON productos(precio) WHERE activo = TRUE;
```

## EXPLAIN ANALYZE

```sql
EXPLAIN ANALYZE
SELECT * FROM productos WHERE nombre = ''Teclado mecanico'';
```', 480, 0, false),

(s4, 'Transacciones y ACID', 'Garantias de integridad con BEGIN, COMMIT y ROLLBACK.', 'text',
'# Transacciones

## Concepto ACID

- **Atomicidad**: Todo o nada
- **Consistencia**: De un estado valido a otro
- **Isolation**: Transacciones no interfieren entre si
- **Durabilidad**: Los cambios persisten

## Uso basico

```sql
BEGIN;

UPDATE cuentas SET saldo = saldo - 100 WHERE id = 1;
UPDATE cuentas SET saldo = saldo + 100 WHERE id = 2;

-- Si todo va bien:
COMMIT;

-- Si algo falla:
ROLLBACK;
```

## SAVEPOINT

```sql
BEGIN;
INSERT INTO pedidos (cliente_id, total) VALUES (1, 150);
SAVEPOINT sp1;

INSERT INTO lineas_pedido (pedido_id, producto_id) VALUES (1, 999);
-- Error: producto 999 no existe
ROLLBACK TO sp1;

-- El pedido sigue insertado, solo se deshace la linea
COMMIT;
```

## Niveles de aislamiento

```sql
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;   -- Default
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
```', 360, 1, false);

END;
$$;
