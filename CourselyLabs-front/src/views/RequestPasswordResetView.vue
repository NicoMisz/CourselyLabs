<template>
  <q-page class="flex flex-center request-reset-page">
    <q-card flat bordered class="q-pa-xl reset-card">
      <template v-if="!sent">
        <h1 class="text-h5 text-center q-mb-md">Recupera tu contraseña</h1>
        <p class="text-body2 text-center text-grey-7 q-mb-lg">
          Introduce tu email y te enviaremos un enlace para restablecer tu contraseña.
        </p>

        <q-form @submit="onSubmit" class="q-gutter-md">
          <q-input
            type="email"
            v-model="email"
            label="Tu email"
            outlined
            lazy-rules
            :rules="[
              v => !!v || 'Introduce tu email',
              v => /.+@.+\..+/.test(v) || 'Introduce un email válido'
            ]"
          />

          <div class="row q-gutter-sm justify-end">
            <q-btn flat color="primary" label="Volver" no-caps @click="goLogin" />
            <q-btn type="submit" unelevated color="primary" label="Enviar enlace" no-caps :loading="loading" />
          </div>
        </q-form>
      </template>

      <template v-else>
        <div class="success-icon q-mx-auto q-mb-md">
          <q-icon name="mail" size="48px" color="white" />
        </div>
        <div class="text-h5 q-mb-sm text-center">Revisa tu email</div>
        <p class="text-body2 text-grey-7 text-center q-mb-lg">
          Si existe una cuenta asociada a <strong>{{ email }}</strong>, te hemos enviado un enlace para restablecer tu contraseña. El enlace expira en 1 hora.
        </p>
        <div class="row justify-center">
          <q-btn unelevated color="primary" label="Volver al inicio" to="/" no-caps />
        </div>
      </template>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useQuasar } from 'quasar'
import api from '@/api/axios'

const router = useRouter()
const $q = useQuasar()

const email = ref('')
const loading = ref(false)
const sent = ref(false)

async function onSubmit() {
  loading.value = true
  try {
    await api.post('/api/auth/request-password-reset', { email: email.value })
    sent.value = true
  } catch {
    $q.notify({ type: 'negative', message: 'Error al enviar el enlace. Inténtalo de nuevo.', position: 'bottom-right' })
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.request-reset-page {
  background: var(--app-bg);
  color: var(--app-text);
  min-height: calc(100vh - 64px);
}

.reset-card {
  width: 100%;
  max-width: 460px;
  background: var(--app-surface);
  color: var(--app-text);
}

.success-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--q-positive);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
