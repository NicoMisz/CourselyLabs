# Recursos descargables y uploads — CourselyLabs

Sistema de upload de archivos via MinIO (S3-compatible). Cubre:

- **Recursos descargables** adjuntos a lecciones (PDFs, docs, ZIPs, imagenes)
- **Thumbnails** de cursos
- **Contenido** de lecciones (video MP4, PDF)

---

## Arquitectura

### Stack

- **Desarrollo**: MinIO en Docker (`minio/minio:latest`, puerto 9000 API + 9001 consola)
- **Produccion**: servidor propio con MinIO, o cualquier S3-compatible (Backblaze B2, Cloudflare R2, AWS S3) — mismo SDK, solo cambian credenciales y endpoint

### Descarga

URLs presignadas de MinIO con expiracion de 15 minutos. El navegador descarga directamente desde MinIO, el backend solo firma la URL. Mas rapido y escala mejor que streaming a traves del backend.

El contador `downloadCount` se incrementa cuando se genera la URL (no cuenta reintentos de red, pero es aproximado — no critico).

### Tipos permitidos

**Recursos descargables y contenido de leccion** (hasta 50 MB por archivo):
- PDF (`application/pdf`)
- Documentos: DOCX, XLSX, PPTX
- Imagenes: JPG, PNG, GIF, WebP, SVG
- Texto/codigo: TXT, MD, CSV, JSON
- Archivos: ZIP

**Thumbnails de curso** (hasta 5 MB):
- JPG, PNG, WebP, GIF

### Limites de almacenamiento por curso

| Rol | Limite |
|---|---|
| `user` | 300 MB |
| `premium` | 1 GB |
| `admin` | ilimitado |

El limite aplica a la suma total de thumbnails + contenido de lecciones + recursos descargables dentro de un mismo curso. Se rastrea en la columna `courses.storage_bytes`.

---

## Esquema de datos

### Tabla `lesson_resources`

| Columna | Tipo | Descripcion |
|---|---|---|
| `id` | UUID | PK |
| `lesson_id` | UUID | FK a lessons (CASCADE) |
| `file_name` | VARCHAR(255) | Nombre original mostrado al usuario |
| `storage_key` | VARCHAR(500) | Clave en MinIO (`resources/{courseId}/{lessonId}/{uuid}-filename`) |
| `file_size` | BIGINT | Bytes |
| `mime_type` | VARCHAR(100) | Content-Type |
| `download_count` | INTEGER | Contador aproximado |
| `position` | INTEGER | Orden dentro de la leccion |
| `created_at` | TIMESTAMP | |

### Columna `courses.storage_bytes`

BIGINT. Suma total de archivos subidos para ese curso. Se actualiza en cada upload/delete.

### Flyway

- `V9__create_lesson_resources.sql`

---

## Endpoints

### Recursos descargables

| Endpoint | Permisos | Descripcion |
|---|---|---|
| `GET /api/lessons/{lessonId}/resources` | `canAccessLesson` | Lista los recursos de una leccion |
| `POST /api/lessons/{lessonId}/resources` | `canEditLesson` | Sube un nuevo recurso (multipart `file`) |
| `GET /api/resources/{id}/download` | `canAccessResource` | Devuelve `{ url }` con URL presignada |
| `DELETE /api/resources/{id}` | `canEditResource` | Elimina el recurso y el archivo |

### Uploads genericos

| Endpoint | Permisos | Descripcion |
|---|---|---|
| `POST /api/courses/{courseId}/thumbnail` | owner/instructor/admin | Sube thumbnail de curso, devuelve `{ url }` presignada |
| `POST /api/lessons/{lessonId}/content-upload` | `canEditLesson` | Sube video/PDF para una leccion, devuelve `{ url }` presignada |

---

## Reglas de acceso (`CourseSecurityService`)

- **`canAccessLesson(lessonId)`**:
  - Leccion con `is_free=true`: cualquier usuario autenticado
  - Leccion no free: owner, instructor, admin, o usuario inscrito
- **`canAccessResource(resourceId)`**: igual que `canAccessLesson` de su leccion padre
- **`canEditLesson` / `canEditResource`**: owner, instructor o admin del curso

El control de acceso para cursos premium ya se aplica en `EnrollmentService` (no te inscribes sin premium), asi que si tienes enrollment, tienes acceso.

---

## Variables de entorno

```bash
# .env.local (backend)
export STORAGE_ENDPOINT=http://localhost:9000
export STORAGE_ACCESS_KEY=minioadmin
export STORAGE_SECRET_KEY=minioadmin
export STORAGE_BUCKET=courselylabs
export STORAGE_PRESIGN_EXPIRY=15   # minutos
```

Para produccion, apuntar a tu MinIO publico o cualquier S3 compatible. El bucket se crea automaticamente al arrancar si no existe.

---

## Componentes frontend

### `FileUploader.vue` — reutilizable

Drag & drop + click. Emite `@upload(file, onProgress)`.

Props: `accept`, `maxSizeMb`, `label`, `hint`, `icon`, `disabled`.

Uso:
```vue
<FileUploader
  ref="uploader"
  accept="image/*"
  :max-size-mb="5"
  label="Subir imagen"
  @upload="handleUpload"
/>
```

El componente expone `finish(errMsg?)` para resetear progreso tras terminar.

### `LessonResources.vue` — vista estudiante

Se muestra en `LessonView.vue` debajo del contenido. Descarga con un click via URL presignada.

---

## Integraciones

- **`CourseContentEditor`** (instructor): en el dialog de editar leccion, uploader de contenido (video/PDF) + lista de recursos descargables + uploader de recurso nuevo.
- **`CourseWizard`** (instructor): reemplaza el input de thumbnail URL por uploader de imagen (solo disponible tras guardar el curso como borrador).
- **`LessonView`** (estudiante): `<LessonResources>` tras el boton "Marcar como completada".

---

## Consideraciones futuras

- [ ] Migrar a Stripe Invoicing para facturas descargables
- [ ] Reordenar recursos dentro de una leccion (drag & drop)
- [ ] Preview inline de PDFs/imagenes en modal antes de descargar (actualmente descarga directa)
- [ ] Virus scan de archivos subidos (ClamAV)
- [ ] Transcodificacion de video (HLS) via FFmpeg en worker
- [ ] CDN delante de MinIO para servir archivos publicos mas rapido
