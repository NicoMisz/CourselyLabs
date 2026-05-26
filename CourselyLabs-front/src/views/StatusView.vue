<template>
  <q-page class="q-pa-md q-pa-lg-lg legal-page">
    <div class="legal-wrap">
      <div class="text-center q-mb-lg">
        <q-icon name="monitor_heart" size="42px" color="primary" />
        <h1 class="text-h4 q-mt-sm q-mb-xs page-title">Estado del servicio</h1>
        <p class="text-body2 page-subtitle">
          Comprobado automáticamente al cargar esta página.
          <q-btn flat dense size="sm" icon="refresh" color="primary" :loading="loading" @click="check" no-caps label="Refrescar" />
        </p>
      </div>

      <q-card flat bordered class="q-mb-lg">
        <q-card-section class="row items-center q-gutter-md">
          <q-spinner v-if="loading" color="primary" size="32px" />
          <q-icon v-else :name="overallIcon" :color="overallColor" size="40px" />
          <div class="col">
            <div class="text-h6">{{ overallLabel }}</div>
            <div class="text-caption text-grey-7">{{ overallDetail }}</div>
          </div>
        </q-card-section>
      </q-card>

      <q-list bordered separator class="bg-transparent">
        <q-item v-for="s in services" :key="s.name">
          <q-item-section avatar>
            <q-icon :name="iconFor(s)" :color="colorFor(s)" size="24px" />
          </q-item-section>
          <q-item-section>
            <q-item-label class="text-weight-medium">{{ s.name }}</q-item-label>
            <q-item-label caption>{{ s.description }}</q-item-label>
          </q-item-section>
          <q-item-section side>
            <q-chip dense :color="colorFor(s)" text-color="white">{{ labelFor(s) }}</q-chip>
          </q-item-section>
        </q-item>
      </q-list>

      <div class="text-caption text-grey-7 q-mt-lg text-center">
        ¿Algún problema que no veas reflejado aquí?
        <router-link to="/contacto">Avísanos</router-link>.
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '@/api/axios'

type Health = 'ok' | 'degraded' | 'down' | 'unknown'
interface Service {
  name: string
  description: string
  status: Health
  check: () => Promise<Health>
}

const loading = ref(false)

const services = ref<Service[]>([
  {
    name: 'API',
    description: 'Backend principal de CourselyLabs',
    status: 'unknown',
    check: async () => {
      try {
        const r = await api.get('/api/categories', { timeout: 4000 })
        return r.status === 200 ? 'ok' : 'degraded'
      } catch { return 'down' }
    },
  },
  {
    name: 'Catálogo de cursos',
    description: 'Listado público de cursos disponibles',
    status: 'unknown',
    check: async () => {
      try {
        const r = await api.get('/api/courses/all', { timeout: 4000 })
        return r.status === 200 ? 'ok' : 'degraded'
      } catch { return 'down' }
    },
  },
  {
    name: 'Pagos (Stripe)',
    description: 'Suscripciones Premium y procesamiento de pagos',
    status: 'unknown',
    check: async () => {
      try {
        const r = await api.get('/api/payments/pricing', { timeout: 5000 })
        const data = r.data || {}
        const hasAmount = data?.monthly?.amount != null || data?.annual?.amount != null
        return hasAmount ? 'ok' : 'degraded'
      } catch { return 'down' }
    },
  },
  {
    name: 'Laboratorios (echo)',
    description: 'Entornos virtuales integrados en lecciones',
    status: 'unknown',
    // No tenemos endpoint de healthcheck público de echo desde aquí; lo dejamos
    // informativo. Si en el futuro hay un /api/labs/health lo cableamos.
    check: async () => 'unknown',
  },
])

async function check() {
  loading.value = true
  await Promise.all(services.value.map(async (s) => {
    s.status = await s.check()
  }))
  loading.value = false
}

function iconFor(s: Service) {
  return s.status === 'ok' ? 'check_circle'
       : s.status === 'degraded' ? 'warning'
       : s.status === 'down' ? 'error'
       : 'help_outline'
}
function colorFor(s: Service) {
  return s.status === 'ok' ? 'positive'
       : s.status === 'degraded' ? 'warning'
       : s.status === 'down' ? 'negative'
       : 'grey-6'
}
function labelFor(s: Service) {
  return s.status === 'ok' ? 'Operativo'
       : s.status === 'degraded' ? 'Degradado'
       : s.status === 'down' ? 'Caído'
       : 'Desconocido'
}

const overallIcon = computed(() => {
  if (services.value.some(s => s.status === 'down')) return 'error'
  if (services.value.some(s => s.status === 'degraded')) return 'warning'
  if (services.value.every(s => s.status === 'ok' || s.status === 'unknown')) return 'check_circle'
  return 'help_outline'
})
const overallColor = computed(() => {
  if (services.value.some(s => s.status === 'down')) return 'negative'
  if (services.value.some(s => s.status === 'degraded')) return 'warning'
  return 'positive'
})
const overallLabel = computed(() => {
  if (services.value.some(s => s.status === 'down')) return 'Hay servicios caídos'
  if (services.value.some(s => s.status === 'degraded')) return 'Servicios con degradación'
  return 'Todos los sistemas operativos'
})
const overallDetail = computed(() => {
  const checkedAt = new Date().toLocaleTimeString('es-ES')
  return `Última comprobación: ${checkedAt}`
})

onMounted(check)
</script>

<style scoped>
@import './_legal.scss';
</style>
