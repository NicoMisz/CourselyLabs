<template>
  <q-page class="q-pa-lg">
    <div style="max-width: 900px; margin: 0 auto">
      <!-- Hero -->
      <div class="text-center q-mb-xl">
        <q-icon name="workspace_premium" size="64px" color="amber-8" />
        <h1 class="text-h4 q-mt-md q-mb-sm" style="font-family: Monda, sans-serif">
          CourselyLabs Premium
        </h1>
        <p class="text-body1 text-grey-7">
          Accede a todos los cursos premium, crea mas cursos y desbloquea funciones exclusivas.
        </p>
      </div>

      <!-- Already premium banner -->
      <q-banner v-if="isPremium" rounded class="bg-amber-1 q-mb-xl" inline-actions>
        <template #avatar><q-icon name="check_circle" color="positive" /></template>
        Ya eres miembro Premium. Gestiona tu suscripcion desde tu perfil.
        <template #action>
          <q-btn flat color="primary" label="Ir a mi perfil" to="/profile" />
        </template>
      </q-banner>

      <!-- Benefits -->
      <div class="row q-gutter-md q-mb-xl">
        <div v-for="benefit in benefits" :key="benefit.title" class="col-12 col-sm-6 col-md-4">
          <q-card flat bordered class="full-height q-pa-md">
            <q-icon :name="benefit.icon" size="36px" :color="benefit.color" class="q-mb-sm" />
            <div class="text-subtitle1 text-weight-medium q-mb-xs">{{ benefit.title }}</div>
            <div class="text-body2 text-grey-7">{{ benefit.description }}</div>
          </q-card>
        </div>
      </div>

      <!-- Pricing -->
      <div class="text-center q-mb-lg">
        <h2 class="text-h5 q-my-none" style="font-family: Monda, sans-serif">Elige tu plan</h2>
      </div>

      <div class="row q-gutter-lg justify-center q-mb-xl">
        <!-- Monthly -->
        <div class="col-12 col-sm-5">
          <q-card flat bordered class="q-pa-lg text-center">
            <div class="text-overline text-grey-6">MENSUAL</div>
            <div class="text-h3 text-weight-bold q-my-md" style="font-family: Monda, sans-serif">
              7<span class="text-h6"> EUR/mes</span>
            </div>
            <q-separator class="q-my-md" />
            <div class="text-body2 text-grey-7 q-mb-lg">Cancela cuando quieras</div>
            <q-btn
              color="primary"
              label="Suscribirme mensual"
              unelevated
              no-caps
              class="full-width"
              :loading="loadingMonthly"
              :disable="isPremium"
              @click="handleCheckout('monthly')"
            />
          </q-card>
        </div>

        <!-- Annual -->
        <div class="col-12 col-sm-5">
          <q-card flat bordered class="q-pa-lg text-center recommended-plan">
            <q-badge floating color="accent" label="Ahorra 29%" />
            <div class="text-overline text-grey-6">ANUAL</div>
            <div class="text-h3 text-weight-bold q-my-md" style="font-family: Monda, sans-serif">
              60<span class="text-h6"> EUR/ano</span>
            </div>
            <div class="text-caption text-positive q-mb-xs">5 EUR/mes efectivos</div>
            <q-separator class="q-my-md" />
            <div class="text-body2 text-grey-7 q-mb-lg">Pago unico anual</div>
            <q-btn
              color="accent"
              label="Suscribirme anual"
              unelevated
              no-caps
              class="full-width"
              :loading="loadingAnnual"
              :disable="isPremium"
              @click="handleCheckout('annual')"
            />
          </q-card>
        </div>
      </div>

      <!-- FAQ -->
      <div class="q-mb-lg">
        <h2 class="text-h5 q-mb-md" style="font-family: Monda, sans-serif">Preguntas frecuentes</h2>
        <q-expansion-item v-for="faq in faqs" :key="faq.q" :label="faq.q" dense header-class="text-weight-medium">
          <q-card flat><q-card-section class="text-body2 text-grey-7">{{ faq.a }}</q-card-section></q-card>
        </q-expansion-item>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { useAuthStore } from '../stores/auth'
import { createCheckout, checkIsPremium } from '../api/payments'

const $q = useQuasar()
const authStore = useAuthStore()
const isPremium = ref(false)
const loadingMonthly = ref(false)
const loadingAnnual = ref(false)

const benefits = [
  {
    icon: 'school',
    color: 'primary',
    title: 'Todos los cursos premium',
    description: 'Acceso ilimitado a todos los cursos de pago de la plataforma. Nuevos cursos cada semana.',
  },
  {
    icon: 'add_circle',
    color: 'accent',
    title: 'Crea hasta 10 cursos',
    description: 'Los usuarios premium pueden crear hasta 10 cursos propios (vs. 2 en el plan gratuito).',
  },
  {
    icon: 'workspace_premium',
    color: 'amber-8',
    title: 'Badge Premium',
    description: 'Muestra tu compromiso con el aprendizaje con un badge exclusivo en tu perfil.',
  },
  {
    icon: 'link',
    color: 'positive',
    title: 'Prerequisitos entre cursos',
    description: 'Crea rutas de aprendizaje enlazando tus cursos con prerequisitos (proximamente).',
  },
  {
    icon: 'support_agent',
    color: 'info',
    title: 'Soporte prioritario',
    description: 'Acceso a soporte prioritario para resolver tus dudas mas rapido.',
  },
  {
    icon: 'update',
    color: 'deep-purple',
    title: 'Acceso anticipado',
    description: 'Se el primero en probar nuevas funciones antes de que esten disponibles para todos.',
  },
]

const faqs = [
  { q: '¿Puedo cancelar en cualquier momento?', a: 'Si, puedes cancelar tu suscripcion cuando quieras desde tu perfil. Mantendras acceso hasta el final del periodo de facturacion.' },
  { q: '¿Que pasa con mis cursos si cancelo?', a: 'Perderas acceso a los cursos premium. Los cursos gratuitos y tu progreso se mantienen.' },
  { q: '¿Puedo cambiar de plan mensual a anual?', a: 'Si, puedes cambiar de plan en cualquier momento. El cambio se aplica en el proximo ciclo de facturacion.' },
  { q: '¿Los pagos son seguros?', a: 'Si, todos los pagos se procesan a traves de Stripe, una de las plataformas de pago mas seguras del mundo.' },
]

async function handleCheckout(plan: 'monthly' | 'annual') {
  if (!authStore.isLoggedIn) {
    $q.notify({ type: 'warning', message: 'Inicia sesion para suscribirte', position: 'bottom-right' })
    return
  }

  const loading = plan === 'monthly' ? loadingMonthly : loadingAnnual
  loading.value = true
  try {
    const url = await createCheckout(plan)
    window.location.href = url
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Error al iniciar el pago'
    $q.notify({ type: 'negative', message: msg, position: 'bottom-right' })
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (authStore.isLoggedIn) {
    isPremium.value = await checkIsPremium().catch(() => false)
  }
})
</script>

<style scoped>
.recommended-plan {
  border: 2px solid #ea580c;
}
</style>
