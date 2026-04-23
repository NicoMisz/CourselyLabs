<template>
  <q-page class="flex flex-center">
    <div class="text-center q-pa-xl" style="max-width: 500px">
      <!-- Loading -->
      <div v-if="confirming">
        <q-spinner-dots color="primary" size="48px" />
        <div class="text-body1 q-mt-md text-grey-7">Confirmando tu pago...</div>
      </div>

      <!-- Error -->
      <div v-else-if="error">
        <q-icon name="error" size="64px" color="negative" />
        <h1 class="text-h5 q-mb-sm q-mt-md">No pudimos confirmar el pago</h1>
        <p class="text-body1 text-grey-7 q-mb-lg">
          {{ error }}
        </p>
        <div class="row q-gutter-md justify-center">
          <q-btn color="primary" label="Reintentar" :loading="confirming" unelevated no-caps @click="confirm" />
          <q-btn outline color="primary" label="Contactar soporte" to="/profile" no-caps />
        </div>
      </div>

      <!-- Success -->
      <div v-else>
        <div class="success-icon q-mx-auto q-mb-lg">
          <q-icon name="check" size="48px" color="white" />
        </div>
        <h1 class="text-h5 q-mb-sm">Bienvenido a Premium!</h1>
        <p class="text-body1 text-grey-7 q-mb-lg">
          Tu suscripcion se ha activado correctamente. Ya tienes acceso a todos los cursos premium.
        </p>
        <div class="row q-gutter-md justify-center">
          <q-btn color="primary" label="Explorar cursos" to="/cursos" unelevated no-caps />
          <q-btn outline color="primary" label="Ir a mi perfil" to="/profile" no-caps />
        </div>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { confirmPayment } from '../api/payments'

const route = useRoute()
const authStore = useAuthStore()
const confirming = ref(true)
const error = ref('')

async function confirm() {
  const sessionId = route.query.session_id as string
  if (!sessionId) {
    error.value = 'No se recibio el identificador de pago.'
    confirming.value = false
    return
  }

  confirming.value = true
  error.value = ''
  try {
    await confirmPayment(sessionId)
    // Refresh session to get updated role
    await authStore.checkSession()
  } catch (err: any) {
    error.value = err?.response?.data?.message || 'Error al confirmar el pago. Intenta nuevamente.'
  } finally {
    confirming.value = false
  }
}

onMounted(confirm)
</script>

<style scoped>
.success-icon {
  width: 88px;
  height: 88px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #059669, #0f766e);
}
</style>
