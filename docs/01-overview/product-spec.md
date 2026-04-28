# Especificación del producto

> Qué hace CourselyLabs y bajo qué reglas de negocio. Este documento es la fuente única para "qué debe hacer la plataforma". Para "qué hay implementado hoy" mira [`state.md`](state.md).

---

## 1. Tipo de cursos y contenido

- **Formatos**: vídeo (MP4 con HLS futuro), texto enriquecido, PDFs, audio (futuro), presentaciones (futuro), código interactivo (futuro).
- **Sin transmisión en vivo** por ahora.
- **Jerarquía**: Curso → Sección → Lección → Bloques de contenido (texto, vídeo, PDF, cuestionario, proyecto, pregunta abierta).
- **Prerequisitos**: un curso puede requerir haber completado otros con un umbral mínimo (`completion_threshold`).
- **Múltiples instructores** por curso.

## 2. Modelo de negocio

- **Suscripción Premium mensual** (no hay compra de cursos individuales).
- **Cursos gratuitos** accesibles para cualquier usuario autenticado.
- **Cursos Premium** solo accesibles para suscriptores Premium.
- **Lecciones gratuitas** (preview): pueden marcarse para que cualquier usuario las vea, incluso en cursos Premium.
- **Cupones de descuento**: planificado, no implementado.
- **Programa de afiliados**: planificado, no implementado.
- **Sin reembolsos** por ahora.

### Límites por rol

| Recurso | `user` | `premium` | `admin` |
|---|---|---|---|
| Cursos como instructor | 2 | 10 | ∞ |
| Storage por curso (vídeo + PDF + recursos) | 300 MB | 1 GB | ∞ |
| Evaluaciones de cada tipo por curso | 2 | ∞ | ∞ |
| Crear prerequisitos entre cursos | ❌ | ✅ | ✅ |
| Crear cursos Premium | ❌ | ✅ | ✅ |

## 3. Sistema de evaluación

- **Tipos de evaluación**:
  - **Cuestionario** (`quiz`): preguntas de respuesta múltiple, autocorregido.
  - **Pregunta abierta** (`open_text`): respuesta libre de hasta 20.000 caracteres, calificación manual.
  - **Proyecto** (`project`): entrega de archivo (máx. 50 MB), calificación manual.
- **Límites por evaluación**: `maxAttempts` (intentos), `timeLimitMinutes` (solo quiz), `passingScore` (0-100).
- **Sin certificados automáticos** por ahora.
- **Las entregas no consumen storage del curso**: los archivos del alumno se almacenan aparte.

## 4. Interacción social

- **Foros** (uno por curso, archivables): planificado, no implementado.
- **Mensajería privada** entre usuarios: planificado, no implementado.
- **Sin comentarios** en momentos específicos del vídeo (solo discusión en foros).
- **Valoraciones** de cursos: backend implementado, frontend mínimo.

## 5. Gestión de usuarios y roles

- **Roles**: `admin`, `premium`, `user`. Cualquier `user` puede crear cursos (es instructor de facto).
- **Verificación**: los cursos públicos requieren aprobación de un administrador antes de publicarse (`status` en `draft`/`pending_review`/`published`/`rejected`).
- **Privacidad**: información de un usuario solo visible para usuarios del mismo curso.
- **Sin gamificación** (badges, puntos): no planificado de momento.

## 6. Análisis y seguimiento

- **Historial de actividad** del usuario (lecciones completadas, intentos, posición en vídeo).
- **Métricas agregadas**: no implementadas.

## 7. Funcionalidades transversales

- **Notificaciones**: planificado para foros seguidos y mensajes directos. No implementado.
- **Recursos descargables**: archivos adjuntos a una lección (PDFs, código, ZIP). Implementado vía MinIO.
- **No implementado**: listas de deseos, notas privadas, marcadores, subtítulos, transcripciones.

## 8. Legal y privacidad

- **Estado actual**: nada específico.
- **Futuro**: historial de transacciones, consentimientos GDPR, política de cookies, etc.

## 9. Multiidioma

- **Estado actual**: solo castellano. Infraestructura `vue-i18n` instalada para añadir locales en el futuro sin refactor.

## 10. Volumetría esperada

- **Escala inicial**: pequeña/mediana. Pocos usuarios.
- **Crecimiento**: arquitectura monolítica modular pensada para evolucionar a microservicios si el tráfico lo justifica.

---

## Reglas de negocio resumidas (cheat sheet)

- Un alumno **no puede inscribirse** en un curso con prerequisitos no cumplidos.
- Un alumno **no puede acceder** al contenido de un curso Premium si no es Premium o no está inscrito (las lecciones marcadas como gratuitas son la excepción).
- Un instructor **no puede publicar** un curso sin pasar por revisión admin.
- Una **evaluación** solo existe vinculada a un bloque assessment-type (`quiz`/`project`/`open_text`) — un bloque que no es de evaluación no puede tener una.
- Un alumno **completa una lección** automáticamente cuando aprueba todas sus evaluaciones (si las tiene) o las marca manualmente (si no).
- Una **suscripción Premium** se gestiona en Stripe. Si caduca, el usuario pierde acceso a cursos Premium pero conserva los gratuitos.
- Un **curso aprobado** que se rechaza después vuelve a `pending_review` (no a `draft`).

---

## Cambios respecto a la spec original

Comparando con `CLAUDE.md` (la primera versión funcional del documento), estas decisiones se han revisado:

| Tema | Original | Realidad actual |
|---|---|---|
| Pago único por curso | Sí, junto con suscripción | Solo suscripción Premium |
| Tipo de lección | Una lección, un tipo | Lección como contenedor de bloques heterogéneos |
| Una evaluación por lección | Sí | Una evaluación por **bloque** (varios bloques por lección) |
| Roles | admin / instructor / estudiante | admin / premium / user (cualquier user es instructor) |
| AWS S3 + Cloudflare CDN | Stack objetivo | MinIO local, sin CDN aún |
| Redis, RabbitMQ, Elasticsearch | Stack objetivo | No implementados (no hay carga que los justifique aún) |
