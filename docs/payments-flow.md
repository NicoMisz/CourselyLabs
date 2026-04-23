# Flujo de pagos — CourselyLabs

Documentacion del flujo de suscripcion Premium via Stripe.

---

## Modelo

- **Cursos gratuitos** (`isFree=true`): cualquier usuario autenticado puede inscribirse
- **Cursos premium** (`isFree=false`): solo usuarios con suscripcion activa o rol admin pueden inscribirse
- **No hay compra individual de cursos** — todo va por suscripcion

### Planes

| Plan | Precio | Periodo |
|---|---|---|
| Mensual | 7 EUR | Cada mes |
| Anual | 60 EUR | Cada ano (equivale a ~5 EUR/mes, -29%) |

### Beneficios Premium

1. Acceso a todos los cursos premium
2. Crear hasta 10 cursos propios (vs. 2 en gratuito)
3. Badge Premium en el perfil
4. Prerequisitos entre cursos (proximamente)
5. Soporte prioritario
6. Acceso anticipado a nuevas funciones

---

## Arquitectura

### Entidades

- **`SubscriptionEntity`**: `id`, `userId`, `stripeSubscriptionId`, `stripeCustomerId`, `plan` (`monthly/annual`), `status` (`active/cancelled/past_due/expired`), `currentPeriodStart/End`, `cancelledAt`
- **`PaymentEntity`**: `id`, `userId`, `stripeSessionId`, `type` (`subscription/one_time`), `description`, `amount`, `currency`, `status` (`pending/completed/failed`)

### Flyway

- `V7__create_subscriptions_and_payments.sql` — crea ambas tablas

---

## Flujo principal (sin dependencia de webhook)

```
  Usuario                 Frontend                  Backend                 Stripe
     |                       |                         |                       |
     |--- Click "Premium" -->|                         |                       |
     |                       |-- POST /payments/       |                       |
     |                       |   checkout {plan} ----->|                       |
     |                       |                         |-- Session.create() -->|
     |                       |                         |<-- session.url -------|
     |                       |<-- { url } -------------|                       |
     |                       |                         |                       |
     |<--- Redirect a Stripe Checkout URL -------------+-----------------------|
     |                                                                         |
     |=== Introduce tarjeta y paga ============================================>|
     |                                                                         |
     |<--- Redirect /pago/exito?session_id=cs_test_xxx ------------------------|
     |                       |                         |                       |
     |                       |-- POST /payments/       |                       |
     |                       |   confirm { sessionId }>|                       |
     |                       |                         |-- Session.retrieve -->|
     |                       |                         |<-- { paid: true } ----|
     |                       |                         |                       |
     |                       |                         | * Crea subscription   |
     |                       |                         | * Crea payment        |
     |                       |                         | * Rol -> "premium"    |
     |                       |<-- SubscriptionDTO ----|                       |
     |                       |                         |                       |
     |                       |-- checkSession() ----->|                       |
     |                       |<-- new JWT (premium) --|                       |
     |                       |                         |                       |
     |<-- "Bienvenido a Premium!" + botones navegacion                         |
```

### Ventajas

- **No depende del webhook**: el usuario activa su suscripcion al volver al sitio
- **Autosuficiente**: sin Stripe CLI en desarrollo, sin URL publica en produccion
- **Idempotente**: si el confirm se llama dos veces, solo crea la suscripcion una vez (check por `stripeSessionId`)

---

## Webhook (respaldo)

El webhook sigue activo como respaldo para eventos que no pasan por el redirect del usuario:

- `invoice.paid` — renovacion automatica mensual/anual
- `customer.subscription.updated` — cambios de plan desde portal Stripe
- `customer.subscription.deleted` — cancelacion desde portal Stripe (pasa a `expired`, rol vuelve a `user`)
- `checkout.session.completed` — redundante con `/confirm`, idempotente

### Configuracion en desarrollo

```bash
# Instalar Stripe CLI
sudo dnf install stripe
# o descargar de github.com/stripe/stripe-cli/releases

stripe login
stripe listen --forward-to localhost:8080/api/payments/webhook
# Copia el whsec_xxx al .env.local
```

### Configuracion en produccion

Dashboard de Stripe -> Developers -> Webhooks -> Add endpoint:
- URL: `https://api.courselylabs.com/api/payments/webhook`
- Eventos: `checkout.session.completed`, `invoice.paid`, `customer.subscription.updated`, `customer.subscription.deleted`
- Copiar el `whsec_xxx` como `STRIPE_WEBHOOK_SECRET`

---

## Endpoints

| Endpoint | Auth | Descripcion |
|---|---|---|
| `POST /api/payments/checkout` | Auth | Crea Stripe Checkout Session. Body: `{ plan: "monthly" \| "annual" }`. Devuelve `{ url }` |
| `POST /api/payments/confirm` | Auth | Confirma pago tras redirect. Body: `{ sessionId }`. Devuelve `SubscriptionDTO` |
| `POST /api/payments/webhook` | Publica | Receptor de webhooks Stripe (verificacion de firma) |
| `GET /api/payments/subscription` | Auth | Suscripcion actual del usuario (ultima creada) |
| `POST /api/payments/cancel-subscription` | Auth | Cancela al final del periodo actual |
| `GET /api/payments/history` | Auth | Historial de pagos del usuario |
| `GET /api/payments/is-premium` | Auth | `{ premium: boolean }` |

---

## Variables de entorno

`CourselyLabs-back/.env.local` (ya en `.gitignore`):

```bash
export STRIPE_SECRET_KEY=sk_test_...
export STRIPE_PUBLIC_KEY=pk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...
export STRIPE_MONTHLY_PRICE_ID=price_...
export STRIPE_ANNUAL_PRICE_ID=price_...
```

Crear los Price IDs en Stripe Dashboard -> Products:
- Producto "CourselyLabs Premium" con dos precios:
  - 7.00 EUR recurring monthly
  - 60.00 EUR recurring yearly

---

## Flujo de cancelacion

1. Usuario entra en `/profile` -> seccion "Suscripcion"
2. Click "Cancelar suscripcion"
3. Backend llama `Subscription.update(id, { cancel_at_period_end: true })`
4. Estado local cambia a `cancelled`, `cancelledAt = now`
5. Usuario mantiene acceso hasta `currentPeriodEnd`
6. Cuando Stripe procesa `customer.subscription.deleted` via webhook -> estado `expired` + rol `user`

---

## Reglas de acceso

`EnrollmentService.enrollCurrentUser` verifica al inscribirse en curso premium:

```java
boolean isPremium = "admin".equals(user.getRole())
    || subscriptionRepository.existsByUserIdAndStatus(user.getId(), "active");
if (!isPremium) {
    throw new BadRequestException("Este curso requiere una suscripcion Premium");
}
```

En el frontend, `CourseSidebar.vue` muestra el boton "Hazte Premium" para cursos premium a usuarios sin suscripcion (redirige a `/premium`).

---

## Tarjetas de prueba Stripe (modo test)

| Escenario | Numero |
|---|---|
| Pago correcto | `4242 4242 4242 4242` |
| Requiere 3DS | `4000 0025 0000 3155` |
| Tarjeta rechazada | `4000 0000 0000 9995` |

Fecha: cualquier futura | CVV: cualquier 3 digitos | CP: cualquier 5 digitos
