<template>
  <q-page class="q-pa-md q-pa-lg-lg legal-page">
    <div class="legal-wrap">
      <div class="text-center q-mb-lg">
        <q-icon name="cookie" size="42px" color="primary" />
        <h1 class="text-h4 q-mt-sm q-mb-xs page-title">Política de cookies</h1>
        <p class="text-body2 page-subtitle">Última actualización: {{ updatedAt }}</p>
      </div>

      <q-banner class="bg-orange-1 text-orange-10 q-mb-lg" rounded dense>
        <template v-slot:avatar>
          <q-icon name="gavel" color="orange-9" />
        </template>
        Esta es una plantilla orientativa; revísala con tu asesor legal y ajusta a los servicios externos que realmente uses.
      </q-banner>

      <section class="prose">
        <h2>1. ¿Qué son las cookies?</h2>
        <p>
          Son pequeños archivos que un sitio web guarda en tu dispositivo para recordar información sobre tu visita (por ejemplo, mantenerte logueado). En CourselyLabs usamos también almacenamiento local del navegador, que cumple una función equivalente.
        </p>

        <h2>2. Cookies que usamos</h2>

        <h3>Imprescindibles</h3>
        <ul>
          <li><strong>Sesión de usuario</strong> — guarda tu JWT para mantenerte logueado. Sin ella no podrías navegar como usuario autenticado.</li>
          <li><strong>Preferencias de UI</strong> — tu elección de modo claro/oscuro y otros ajustes visuales.</li>
        </ul>

        <h3>Funcionales y analíticas (futuro)</h3>
        <ul>
          <li>Por ahora <strong>no usamos cookies de analítica</strong> (Google Analytics u otras).</li>
          <li>Si las añadimos, te pediremos consentimiento expreso con un banner antes de cargar nada.</li>
        </ul>

        <h3>De terceros</h3>
        <ul>
          <li><strong>Stripe</strong> usa cookies propias durante el proceso de pago en su dominio (<code>checkout.stripe.com</code>). No tenemos control sobre ellas; consulta su <a href="https://stripe.com/cookies-policy/legal" target="_blank" rel="noopener">política de cookies</a>.</li>
        </ul>

        <h2>3. ¿Cómo desactivarlas?</h2>
        <p>
          Puedes configurar tu navegador para bloquear o eliminar cookies (busca en su menú de Privacidad). Ten en cuenta que si bloqueas las imprescindibles no podrás iniciar sesión.
        </p>

        <h2>4. Cambios en esta política</h2>
        <p>
          Si añadimos nuevas categorías de cookies (analíticas, publicidad, etc.), actualizaremos esta página y mostraremos un banner de consentimiento.
        </p>
      </section>

      <div class="text-center q-mt-xl text-grey-7 text-caption">
        ¿Dudas? Escríbenos a <a href="mailto:privacidad@courselylabs.com">privacidad@courselylabs.com</a>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
const updatedAt = '14 de mayo de 2026'
</script>

<style scoped>
@import './_legal.scss';
</style>
