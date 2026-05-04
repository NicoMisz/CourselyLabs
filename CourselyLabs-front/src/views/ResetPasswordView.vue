<template>
  <q-page class="flex flex-center reset-page">
    <q-card flat bordered class="q-pa-xl reset-card">
      <template v-if="!token">
        <q-icon name="error_outline" size="64px" color="negative" class="q-mb-md block-center" />
        <div class="text-h5 q-mb-sm text-center">Enlace no válido</div>
        <p class="text-body2 text-grey-7 text-center q-mb-lg">
          El enlace para restablecer la contraseña no es válido. Solicita uno nuevo.
        </p>
        <div class="row justify-center">
          <q-btn unelevated color="primary" label="Solicitar nuevo enlace" to="/recuperar-contrasena" no-caps />
        </div>
      </template>

      <template v-else-if="!done">
        <h1 class="text-h5 text-center q-mb-md">Nueva contraseña</h1>
        <p class="text-body2 text-center text-grey-7 q-mb-lg">
          Introduce tu nueva contraseña.
        </p>

        <q-form @submit="onSubmit" class="q-gutter-md">
          <q-input
            v-model="newPassword"
            label="Nueva contraseña"
            :type="showPassword ? 'text' : 'password'"
            outlined
            lazy-rules
            :rules="[
              v => !!v || 'Introduce una contraseña',
              v => v.length >= 8 || 'Mínimo 8 caracteres'
            ]"
          >
            <template v-slot:append>
              <q-icon
                :name="showPassword ? 'visibility_off' : 'visibility'"
                class="cursor-pointer"
                @click="showPassword = !showPassword"
              />
            </template>
          </q-input>

          <q-input
            v-model="confirmPassword"
            label="Confirma la contraseña"
            :type="showPassword ? 'text' : 'password'"
            outlined
            lazy-rules
            :rules="[
              v => !!v || 'Confirma la contraseña',
              v => v === newPassword || 'Las contraseñas no coinciden'
            ]"
          />

          <div class="row q-gutter-sm justify-end">
            <q-btn type="submit" unelevated color="primary" label="Cambiar contraseña" no-caps :loading="loading" />
          </div>
        </q-form>
      </template>

      <template v-else>
        <div class="success-icon q-mx-auto q-mb-md">
          <q-icon name="check" size="48px" color="white" />
        </div>
        <div class="text-h5 q-mb-sm text-center">Contraseña actualizada</div>
        <p class="text-body2 text-grey-7 text-center q-mb-lg">
          Tu contraseña se ha cambiado correctamente. Ya puedes iniciar sesión con la nueva.
        </p>
        <div class="row justify-center">
          <q-btn unelevated color="primary" label="Iniciar sesión" to="/login" no-caps />
        </div>
      </template>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useQuasar } from 'quasar'
import api from '@/api/axios'

const route = useRoute()
const $q = useQuasar()

const token = computed(() => {
  const t = route.query.token
  return typeof t === 'string' && t.length > 0 ? t : ''
})

const newPassword = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const loading = ref(false)
const done = ref(false)

async function onSubmit() {
  loading.value = true
  try {
    await api.post('/api/auth/reset-password', {
      token: token.value,
      newPassword: newPassword.value,
    })
    done.value = true
  } catch (err: unknown) {
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      ?? 'Error al cambiar la contraseña. El enlace puede haber expirado.'
    $q.notify({ type: 'negative', message, position: 'bottom-right' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-page {
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

.block-center {
  display: block;
  margin: 0 auto;
}
</style>
