<template>
  <q-card flat bordered class="lab-block">
    <q-card-section class="row items-center q-gutter-sm lab-block__header">
      <q-icon name="terminal" color="primary" size="22px" />
      <div class="text-subtitle1 text-weight-medium col">Laboratorio (entorno virtual)</div>
      <q-chip dense :color="chipColor" text-color="white">{{ chipLabel }}</q-chip>
    </q-card-section>

    <q-card-section v-if="instructions" class="rich-content" v-html="renderedInstructions" />

    <q-card-section v-if="loading">
      <q-skeleton type="rect" height="80px" />
    </q-card-section>

    <q-card-section v-else>
      <!-- Sin token de echo -->
      <q-banner v-if="state === 'NO_TOKEN'" class="bg-orange-1 text-orange-10" rounded>
        <template v-slot:avatar>
          <q-icon name="link_off" color="orange-9" />
        </template>
        Conecta tu cuenta de echo desde tu perfil para usar este laboratorio.
        <template v-slot:action>
          <q-btn flat color="primary" label="Ir al perfil" no-caps to="/profile" />
        </template>
      </q-banner>

      <!-- Sin VM asignada -->
      <q-banner v-else-if="state === 'NO_VM'" class="bg-blue-1 text-blue-10" rounded>
        <template v-slot:avatar>
          <q-icon name="info" color="blue-9" />
        </template>
        {{ status?.message || 'No tienes una VM asignada para este laboratorio. Pide a tu instructor que te asigne una.' }}
      </q-banner>

      <!-- VM disponible -->
      <template v-else-if="state === 'STOPPED' || state === 'RUNNING'">
        <div class="row items-center q-gutter-sm">
          <div class="text-body2 col">
            <strong>VM</strong>: {{ status?.vmName }}
            <span class="text-grey-7">·</span>
            <strong>Estado</strong>: {{ stateLabel }}
          </div>
          <q-btn
            v-if="state === 'STOPPED'"
            unelevated
            color="primary"
            icon="play_arrow"
            label="Iniciar laboratorio"
            no-caps
            :loading="busy"
            @click="onStart"
          />
          <template v-else>
            <q-btn
              outline
              color="primary"
              icon="open_in_full"
              label="Abrir consola"
              no-caps
              :loading="consoleLoading"
              @click="onConsole"
            />
            <q-btn
              outline
              color="negative"
              icon="stop"
              label="Detener"
              no-caps
              :loading="busy"
              @click="onStop"
            />
          </template>
        </div>

        <!-- Consola embebida -->
        <div v-if="consoleUrl" class="lab-block__console q-mt-md">
          <div class="row items-center q-mb-xs">
            <div class="text-caption text-grey-7 col">Consola noVNC</div>
            <q-btn flat dense icon="close" size="sm" @click="consoleUrl = ''">
              <q-tooltip>Cerrar consola</q-tooltip>
            </q-btn>
          </div>
          <iframe
            :src="consoleUrl"
            class="lab-block__iframe"
            allow="clipboard-read; clipboard-write"
          />
        </div>
      </template>

      <!-- Error -->
      <q-banner v-else class="bg-red-1 text-red-10" rounded>
        <template v-slot:avatar>
          <q-icon name="error_outline" color="negative" />
        </template>
        {{ status?.message || 'Error al consultar el laboratorio.' }}
        <template v-slot:action>
          <q-btn flat color="primary" label="Reintentar" no-caps :loading="loading" @click="() => refresh()" />
        </template>
      </q-banner>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useQuasar } from 'quasar'
import { marked } from 'marked'
import {
  getLabStatus,
  startLab,
  stopLab,
  openLabConsole,
  type LabStatus,
} from '@/api/lab'

const props = defineProps<{
  blockId: string
  instructions?: string
}>()

const $q = useQuasar()

const status = ref<LabStatus | null>(null)
const loading = ref(true)
const busy = ref(false)
const consoleLoading = ref(false)
const consoleUrl = ref('')
let pollTimer: ReturnType<typeof setInterval> | null = null

const state = computed(() => status.value?.state ?? 'ERROR')

const stateLabel = computed(() => {
  switch (state.value) {
    case 'RUNNING': return 'En ejecución'
    case 'STOPPED': return 'Apagada'
    case 'NO_VM': return 'Sin asignar'
    case 'NO_TOKEN': return 'Sin conectar'
    default: return 'Error'
  }
})

const chipColor = computed(() => {
  switch (state.value) {
    case 'RUNNING': return 'positive'
    case 'STOPPED': return 'grey-6'
    case 'NO_VM': return 'blue-7'
    case 'NO_TOKEN': return 'orange-8'
    default: return 'negative'
  }
})

const chipLabel = computed(() => stateLabel.value)

const renderedInstructions = computed(() => {
  return props.instructions ? marked.parse(props.instructions, { async: false }) as string : ''
})

async function refresh({ silent = false } = {}) {
  if (!silent) loading.value = true
  try {
    status.value = await getLabStatus(props.blockId)
  } catch {
    status.value = { state: 'ERROR', message: 'No se pudo consultar el laboratorio.' }
  } finally {
    if (!silent) loading.value = false
  }
}

async function onStart() {
  busy.value = true
  try {
    status.value = await startLab(props.blockId)
    if (state.value === 'RUNNING') {
      $q.notify({ type: 'positive', message: 'Laboratorio iniciado', position: 'bottom-right' })
      // Abre consola automáticamente — el alumno casi siempre la quiere tras iniciar.
      await onConsole()
    } else if (status.value?.message) {
      $q.notify({ type: 'warning', message: status.value.message, position: 'bottom-right' })
    }
  } catch {
    $q.notify({ type: 'negative', message: 'Error al iniciar el laboratorio', position: 'bottom-right' })
  } finally {
    busy.value = false
  }
}

async function onStop() {
  busy.value = true
  try {
    status.value = await stopLab(props.blockId)
    consoleUrl.value = ''
    $q.notify({ type: 'info', message: 'Laboratorio detenido', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al detener el laboratorio', position: 'bottom-right' })
  } finally {
    busy.value = false
  }
}

async function onConsole() {
  consoleLoading.value = true
  try {
    const result = await openLabConsole(props.blockId)
    if (!result.ok || !result.ticket) {
      $q.notify({ type: 'negative', message: result.error || 'No se pudo abrir la consola', position: 'bottom-right' })
      return
    }
    consoleUrl.value = buildConsoleUrl(result.ticket)
  } catch {
    $q.notify({ type: 'negative', message: 'Error al abrir la consola', position: 'bottom-right' })
  } finally {
    consoleLoading.value = false
  }
}

/**
 * El backend devuelve una URL absoluta al vm_viewer.php de echo lista para iframe
 * (incluye token efímero y ?embedded=1). Se usa directamente.
 */
function buildConsoleUrl(ticket: Record<string, unknown>): string {
  if (typeof ticket.url === 'string') return ticket.url
  return ''
}

onMounted(() => {
  refresh()
  // Polling suave cada 30s para reflejar cambios externos.
  // Se pausa cuando hay consola abierta para no recargar el iframe.
  pollTimer = setInterval(() => {
    if (!consoleUrl.value) refresh({ silent: true })
  }, 30000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.lab-block {
  background: var(--app-surface);
  color: var(--app-text);
}
.lab-block__header {
  border-bottom: 1px solid var(--app-border);
}
.lab-block__console {
  border: 1px solid var(--app-border);
  border-radius: 6px;
  padding: 8px;
  background: var(--app-bg-soft);
}
.lab-block__iframe {
  width: 100%;
  height: 600px;
  border: 0;
  border-radius: 4px;
  background: #000;
}
.rich-content :deep(p) { margin: 0.25rem 0; }
.rich-content :deep(code) { background: var(--app-bg-soft); padding: 1px 4px; border-radius: 3px; }
</style>
