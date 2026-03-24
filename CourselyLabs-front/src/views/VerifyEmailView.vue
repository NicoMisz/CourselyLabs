<template>
  <q-page class="flex flex-center">
    <q-card flat class="text-center q-pa-xl" style="max-width: 460px">
      <!-- Verificando -->
      <template v-if="loading">
        <q-spinner-dots size="48px" color="primary" class="q-mb-md" />
        <div class="text-h6">Verificando tu cuenta...</div>
      </template>

      <!-- Exito -->
      <template v-else-if="success">
        <div class="success-icon q-mx-auto q-mb-md">
          <q-icon name="check" size="48px" color="white" />
        </div>
        <div class="text-h5 q-mb-sm">Cuenta verificada</div>
        <div class="text-body2 text-grey-7 q-mb-lg">
          Tu cuenta ha sido verificada correctamente. Ya puedes disfrutar de todas las funcionalidades.
        </div>
        <q-btn unelevated color="primary" label="Ir al inicio" to="/" no-caps />
      </template>

      <!-- Error -->
      <template v-else>
        <q-icon name="error_outline" size="64px" color="negative" class="q-mb-md" />
        <div class="text-h5 q-mb-sm">No se pudo verificar</div>
        <div class="text-body2 text-grey-7 q-mb-lg">{{ errorMsg }}</div>
        <div class="q-gutter-sm">
          <q-btn
            v-if="showResend"
            unelevated
            color="primary"
            label="Reenviar email"
            no-caps
            :loading="resending"
            @click="resendEmail"
          />
          <q-btn flat color="primary" label="Volver al inicio" to="/" no-caps />
        </div>
      </template>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useQuasar } from 'quasar'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'

const route = useRoute()
const $q = useQuasar()
const authStore = useAuthStore()

const loading = ref(true)
const success = ref(false)
const errorMsg = ref('')
const showResend = ref(false)
const resending = ref(false)

onMounted(async () => {
  const token = route.query.token as string
  if (!token) {
    loading.value = false
    errorMsg.value = 'Enlace de verificacion invalido.'
    return
  }

  try {
    await api.get(`/api/auth/verify?token=${token}`)
    success.value = true

    if (authStore.user) {
      authStore.updateUser({ ...authStore.user, isVerified: true })
    }
  } catch (err: unknown) {
    const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
    errorMsg.value = msg || 'El enlace de verificacion es invalido o ha expirado.'
    showResend.value = true
  } finally {
    loading.value = false
  }
})

async function resendEmail() {
  const email = authStore.user?.email
  if (!email) {
    $q.notify({ type: 'negative', message: 'Inicia sesion para reenviar el email', position: 'bottom-right' })
    return
  }

  resending.value = true
  try {
    await api.post(`/api/auth/resend-verification?email=${encodeURIComponent(email)}`)
    $q.notify({ type: 'positive', message: 'Email de verificacion reenviado', position: 'bottom-right' })
    showResend.value = false
  } catch {
    $q.notify({ type: 'negative', message: 'No se pudo reenviar el email', position: 'bottom-right' })
  } finally {
    resending.value = false
  }
}
</script>

<style scoped>
.success-icon {
  width: 80px;
  height: 80px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f766e, #0d9488);
}
</style>
