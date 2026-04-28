<template>
  <q-page>
    <!-- Hero con gradiente -->
    <section class="hero-section">
      <div class="hero-content text-center">
        <q-icon name="workspace_premium" size="72px" color="amber-4" class="q-mb-md" />
        <h1 class="text-h3 q-my-none text-white" style="font-family: Monda, sans-serif">
          CourselyLabs Premium
        </h1>
        <p class="text-body1 q-mt-md hero-subtitle">
          Accede a todos los cursos premium, crea más cursos y desbloquea funciones exclusivas.
        </p>
      </div>
    </section>

    <div class="content-wrap q-px-md q-px-lg-lg q-pb-xl">
      <!-- Already premium banner -->
      <q-banner v-if="isPremium" rounded class="bg-amber-1 q-mb-xl q-mt-lg" inline-actions>
        <template #avatar><q-icon name="check_circle" color="positive" size="28px" /></template>
        <div class="text-body1 text-weight-medium">Ya eres miembro Premium</div>
        <div class="text-body2 text-grey-7">Gestiona tu suscripción desde tu perfil.</div>
        <template #action>
          <q-btn flat color="primary" label="Ir a mi perfil" to="/profile" no-caps />
        </template>
      </q-banner>

      <!-- Pricing (arriba para CTA rápido) -->
      <div class="text-center q-mt-xl q-mb-lg">
        <h2 class="text-h4 q-my-none" style="font-family: Monda, sans-serif">Elige tu plan</h2>
        <p class="text-body2 text-grey-7 q-mt-sm">Cancela cuando quieras. Sin compromiso.</p>
      </div>

      <div class="pricing-grid q-mb-xl">
        <!-- Monthly -->
        <q-card flat bordered class="plan-card">
          <q-card-section class="text-center">
            <div class="text-overline text-grey-6">MENSUAL</div>
            <div class="q-my-md">
              <span class="text-h3 text-weight-bold" style="font-family: Monda, sans-serif">
                {{ formatPrice(pricing?.monthly?.amount, 7) }}
              </span>
              <span class="text-subtitle1 text-grey-7"> EUR/mes</span>
            </div>
            <q-separator class="q-my-md" />
            <div class="text-body2 text-grey-7 q-mb-lg">Perfecto para probar Premium</div>
            <q-btn
              color="primary"
              label="Empezar mensual"
              unelevated
              no-caps
              class="full-width q-py-sm"
              size="md"
              :loading="loadingMonthly"
              :disable="isPremium"
              @click="handleCheckout('monthly')"
            />
          </q-card-section>
        </q-card>

        <!-- Annual -->
        <q-card flat bordered class="plan-card recommended-plan">
          <div class="recommended-badge">RECOMENDADO · Ahorra {{ savingsPct }}%</div>
          <q-card-section class="text-center">
            <div class="text-overline text-grey-6">ANUAL</div>
            <div class="q-my-md">
              <span class="text-h3 text-weight-bold" style="font-family: Monda, sans-serif">
                {{ formatPrice(pricing?.annual?.amount, 60) }}
              </span>
              <span class="text-subtitle1 text-grey-7"> EUR/ano</span>
            </div>
            <div class="text-caption text-positive q-mb-xs">
              Solo {{ monthlyEquivalent }} EUR/mes efectivos
            </div>
            <q-separator class="q-my-md" />
            <div class="text-body2 text-grey-7 q-mb-lg">La mejor oferta</div>
            <q-btn
              color="accent"
              label="Empezar anual"
              unelevated
              no-caps
              class="full-width q-py-sm"
              size="md"
              :loading="loadingAnnual"
              :disable="isPremium"
              @click="handleCheckout('annual')"
            />
          </q-card-section>
        </q-card>
      </div>

      <!-- Benefits -->
      <div class="text-center q-my-lg">
        <h2 class="text-h4 q-my-none" style="font-family: Monda, sans-serif">Todo lo que incluye</h2>
        <p class="text-body2 text-grey-7 q-mt-sm">Beneficios para disfrutar y crear sin límites.</p>
      </div>

      <div class="benefits-grid q-mb-xl">
        <div v-for="benefit in benefits" :key="benefit.title" class="benefit-card">
          <div class="benefit-icon" :style="{ background: benefit.bg }">
            <q-icon :name="benefit.icon" size="28px" color="white" />
          </div>
          <div class="text-subtitle1 text-weight-medium q-mt-md q-mb-xs">{{ benefit.title }}</div>
          <div class="text-body2 text-grey-7">{{ benefit.description }}</div>
        </div>
      </div>

      <!-- Comparison table -->
      <div class="text-center q-mb-lg">
        <h2 class="text-h4 q-my-none" style="font-family: Monda, sans-serif">Gratis vs Premium</h2>
      </div>
      <q-card flat bordered class="q-mb-xl">
        <q-markup-table flat>
          <thead>
            <tr>
              <th class="text-left">Función</th>
              <th class="text-center">Gratis</th>
              <th class="text-center text-amber-8">
                <q-icon name="workspace_premium" size="16px" /> Premium
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in comparison" :key="row.feature">
              <td class="text-weight-medium">{{ row.feature }}</td>
              <td class="text-center">
                <q-icon v-if="row.free === true" name="check" color="positive" size="20px" />
                <q-icon v-else-if="row.free === false" name="close" color="grey-5" size="20px" />
                <span v-else class="text-body2 text-grey-7">{{ row.free }}</span>
              </td>
              <td class="text-center">
                <q-icon v-if="row.premium === true" name="check_circle" color="amber-8" size="20px" />
                <q-icon v-else-if="row.premium === false" name="close" color="grey-5" size="20px" />
                <span v-else class="text-body2 text-amber-8 text-weight-medium">{{ row.premium }}</span>
              </td>
            </tr>
          </tbody>
        </q-markup-table>
      </q-card>

      <!-- FAQ -->
      <div class="text-center q-mb-md">
        <h2 class="text-h4 q-my-none" style="font-family: Monda, sans-serif">Preguntas frecuentes</h2>
      </div>
      <q-card flat bordered class="q-mb-xl">
        <q-expansion-item
          v-for="faq in faqs"
          :key="faq.q"
          :label="faq.q"
          header-class="text-weight-medium text-body1"
        >
          <q-card flat>
            <q-card-section class="text-body2 text-grey-7">{{ faq.a }}</q-card-section>
          </q-card>
          <q-separator />
        </q-expansion-item>
      </q-card>

      <!-- Trust -->
      <div class="text-center text-grey-6">
        <q-icon name="lock" size="20px" class="q-mr-xs" />
        <span class="text-caption">Pago 100% seguro procesado por Stripe</span>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { useAuthStore } from '../stores/auth'
import { createCheckout, checkIsPremium, getPricing } from '../api/payments'
import type { PricingInfo } from '../api/payments'

const $q = useQuasar()
const authStore = useAuthStore()
const isPremium = ref(false)
const loadingMonthly = ref(false)
const loadingAnnual = ref(false)
const pricing = ref<PricingInfo | null>(null)

const monthlyEquivalent = computed(() => {
  const annual = pricing.value?.annual?.amount ?? 60
  return (annual / 12).toFixed(2)
})

const savingsPct = computed(() => {
  const monthly = pricing.value?.monthly?.amount ?? 7
  const annual = pricing.value?.annual?.amount ?? 60
  const annualFromMonthly = monthly * 12
  if (annualFromMonthly === 0) return 0
  return Math.round(((annualFromMonthly - annual) / annualFromMonthly) * 100)
})

function formatPrice(amount: number | null | undefined, fallback: number): string {
  const n = amount ?? fallback
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

const benefits = [
  {
    icon: 'school',
    bg: 'linear-gradient(135deg, #0f766e, #14b8a6)',
    title: 'Todos los cursos premium',
    description: 'Acceso ilimitado a todos los cursos Premium. Nuevos cursos cada semana.',
  },
  {
    icon: 'add_circle',
    bg: 'linear-gradient(135deg, #ea580c, #f59e0b)',
    title: 'Crea hasta 10 cursos',
    description: 'Comparte tu conocimiento con 5 veces más cursos propios que en gratuito.',
  },
  {
    icon: 'workspace_premium',
    bg: 'linear-gradient(135deg, #d97706, #fbbf24)',
    title: 'Badge Premium',
    description: 'Muestra tu compromiso con un badge exclusivo en tu perfil.',
  },
  {
    icon: 'account_tree',
    bg: 'linear-gradient(135deg, #059669, #10b981)',
    title: 'Rutas de aprendizaje',
    description: 'Enlaza tus cursos con prerequisitos para crear rutas (proximamente).',
  },
  {
    icon: 'support_agent',
    bg: 'linear-gradient(135deg, #0891b2, #06b6d4)',
    title: 'Soporte prioritario',
    description: 'Respuestas rápidas de nuestro equipo cuando las necesites.',
  },
  {
    icon: 'rocket_launch',
    bg: 'linear-gradient(135deg, #7c3aed, #a78bfa)',
    title: 'Acceso anticipado',
    description: 'Prueba nuevas funciones antes que nadie en beta exclusiva.',
  },
]

const comparison = [
  { feature: 'Cursos gratuitos', free: true, premium: true },
  { feature: 'Cursos premium', free: false, premium: true },
  { feature: 'Cursos propios', free: '2 máximo', premium: '10 máximo' },
  { feature: 'Rutas de aprendizaje', free: false, premium: true },
  { feature: 'Badge Premium', free: false, premium: true },
  { feature: 'Soporte prioritario', free: false, premium: true },
  { feature: 'Acceso anticipado', free: false, premium: true },
]

const faqs = [
  { q: '¿Puedo cancelar en cualquier momento?', a: 'Sí, puedes cancelar tu suscripción cuando quieras desde tu perfil. Mantendrás acceso hasta el final del periodo de facturación.' },
  { q: '¿Que pasa con mis cursos si cancelo?', a: 'Perderas acceso a los cursos premium. Los cursos gratuitos, tu progreso y los cursos que hayas creado se mantienen.' },
  { q: '¿Los pagos son seguros?', a: 'Sí, todos los pagos se procesan a través de Stripe, una de las plataformas de pago más seguras y utilizadas del mundo.' },
  { q: '¿Puedo cambiar de plan mensual a anual?', a: 'Sí, contacta con soporte y te ayudaremos a cambiar de plan.' },
]

async function handleCheckout(plan: 'monthly' | 'annual') {
  if (!authStore.isLoggedIn) {
    $q.notify({ type: 'warning', message: 'Inicia sesión para suscribirte', position: 'bottom-right' })
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
  const promises: Promise<void>[] = [
    getPricing().then(p => { pricing.value = p }).catch(() => {}),
  ]
  if (authStore.isLoggedIn) {
    promises.push(checkIsPremium().then(v => { isPremium.value = v }).catch(() => {}))
  }
  await Promise.all(promises)
})
</script>

<style scoped>
.hero-section {
  background: linear-gradient(135deg, #0f766e 0%, #115e59 50%, #d97706 100%);
  padding: 4rem 1.5rem;
  position: relative;
  overflow: hidden;
}

.hero-content {
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-subtitle {
  color: rgba(255, 255, 255, 0.9);
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.content-wrap {
  max-width: 1000px;
  margin: 0 auto;
}

.pricing-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  max-width: 720px;
  margin: 0 auto;
}

.plan-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
}

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.recommended-plan {
  border: 2px solid #ea580c;
  box-shadow: 0 8px 20px rgba(234, 88, 12, 0.15);
}

.recommended-badge {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #ea580c;
  color: white;
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.benefit-card {
  padding: 1.5rem;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.benefit-card:hover {
  border-color: #0f766e;
  transform: translateY(-2px);
}

.benefit-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
