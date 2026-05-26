<template>
  <q-card flat bordered class="echo-card">
    <q-card-section>
      <div class="row items-center q-gutter-sm">
        <q-icon name="science" size="24px" color="primary" />
        <div class="text-h6 col">Cuenta de echo (laboratorios)</div>
        <q-chip
          dense
          :color="status?.connected ? 'positive' : 'grey-5'"
          text-color="white"
        >
          {{ status?.connected ? 'Conectado' : 'No conectado' }}
        </q-chip>
      </div>
      <p class="text-body2 text-grey-7 q-mt-sm q-mb-none">
        Algunos cursos incluyen laboratorios sobre máquinas virtuales gestionadas por echo. Conecta tu cuenta para poder iniciarlas desde aquí, sin abrir echo aparte.
      </p>
    </q-card-section>

    <q-card-section v-if="loading">
      <q-skeleton type="rect" height="60px" />
    </q-card-section>

    <q-card-section v-else-if="status?.connected">
      <div class="text-body2">
        <strong>Usuario echo</strong>: {{ status.username }} <span class="text-grey-7">·</span>
        <strong>Rol</strong>: {{ status.roleName || '—' }}
      </div>
      <div class="row q-gutter-sm q-mt-md">
        <q-btn flat dense color="primary" icon="refresh" label="Refrescar" no-caps :loading="refreshing" @click="refresh" />
        <q-space />
        <q-btn outline color="negative" icon="link_off" label="Desconectar" no-caps :loading="disconnecting" @click="onDisconnect" />
      </div>
    </q-card-section>

    <q-card-section v-else>
      <q-banner v-if="status?.error" class="bg-orange-1 text-orange-10 q-mb-md" rounded dense>
        {{ status.error }}
      </q-banner>

      <p class="text-caption text-grey-7 q-mb-sm">
        Genera un token desde echo (<code>API Docs → My tokens</code>) y pégalo aquí. Tu token se guarda cifrado y nunca se muestra al frontend.
      </p>

      <q-form @submit.prevent="onConnect" class="q-gutter-md">
        <q-input
          v-model="token"
          label="Token de echo"
          outlined
          dense
          :type="showToken ? 'text' : 'password'"
          :rules="[v => !!v && v.length >= 32 || 'Token con formato inesperado']"
          hint="64 caracteres hexadecimales"
        >
          <template v-slot:append>
            <q-icon
              :name="showToken ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="showToken = !showToken"
            />
          </template>
        </q-input>

        <q-btn
          type="submit"
          unelevated
          color="primary"
          icon="link"
          label="Conectar con echo"
          no-caps
          :loading="connecting"
        />
      </q-form>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { getEchoStatus, connectEcho, disconnectEcho, type EchoConnectionStatus } from '@/api/lab'

const $q = useQuasar()

const loading = ref(true)
const refreshing = ref(false)
const connecting = ref(false)
const disconnecting = ref(false)
const showToken = ref(false)
const token = ref('')
const status = ref<EchoConnectionStatus | null>(null)

async function refresh() {
  refreshing.value = true
  try {
    status.value = await getEchoStatus()
  } catch {
    $q.notify({ type: 'negative', message: 'No se pudo consultar el estado de echo', position: 'bottom-right' })
  } finally {
    refreshing.value = false
    loading.value = false
  }
}

async function onConnect() {
  if (!token.value || token.value.length < 32) return
  connecting.value = true
  try {
    status.value = await connectEcho(token.value)
    token.value = ''
    $q.notify({ type: 'positive', message: 'Cuenta de echo conectada', position: 'bottom-right' })
  } catch (err: unknown) {
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      ?? 'No se pudo conectar con echo'
    $q.notify({ type: 'negative', message, position: 'bottom-right' })
  } finally {
    connecting.value = false
  }
}

async function onDisconnect() {
  disconnecting.value = true
  try {
    await disconnectEcho()
    status.value = { connected: false }
    $q.notify({ type: 'info', message: 'Cuenta de echo desconectada', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'No se pudo desconectar', position: 'bottom-right' })
  } finally {
    disconnecting.value = false
  }
}

onMounted(refresh)
</script>

<style scoped>
.echo-card {
  background: var(--app-surface);
  color: var(--app-text);
}
</style>
