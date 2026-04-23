<template>
  <q-card v-if="resources.length > 0 || loading" flat bordered class="q-mb-md">
    <q-card-section>
      <div class="row items-center q-mb-md">
        <q-icon name="attach_file" size="20px" color="primary" class="q-mr-sm" />
        <div class="text-subtitle1 text-weight-medium">Recursos descargables</div>
      </div>

      <div v-if="loading">
        <q-skeleton v-for="n in 2" :key="n" type="rect" height="40px" class="q-mb-sm" />
      </div>

      <q-list v-else separator dense>
        <q-item
          v-for="r in resources"
          :key="r.id"
          clickable
          @click="download(r)"
        >
          <q-item-section avatar>
            <q-icon :name="resourceIcon(r.mimeType)" :color="resourceColor(r.mimeType)" size="28px" />
          </q-item-section>
          <q-item-section>
            <q-item-label>{{ r.fileName }}</q-item-label>
            <q-item-label caption>
              {{ formatFileSize(r.fileSize) }}
              <span v-if="r.downloadCount > 0"> · {{ r.downloadCount }} descargas</span>
            </q-item-label>
          </q-item-section>
          <q-item-section side>
            <q-btn
              flat
              dense
              round
              icon="download"
              color="primary"
              :loading="downloadingId === r.id"
            />
          </q-item-section>
        </q-item>
      </q-list>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useQuasar } from 'quasar'
import {
  listResources,
  getDownloadUrl,
  formatFileSize,
  resourceIcon,
  resourceColor,
} from '../api/resources'
import type { LessonResource } from '../api/resources'

const props = defineProps<{ lessonId: string }>()

const $q = useQuasar()
const resources = ref<LessonResource[]>([])
const loading = ref(true)
const downloadingId = ref<string | null>(null)

async function load() {
  loading.value = true
  try {
    resources.value = await listResources(props.lessonId)
  } catch {
    resources.value = []
  } finally {
    loading.value = false
  }
}

async function download(r: LessonResource) {
  downloadingId.value = r.id
  try {
    const url = await getDownloadUrl(r.id)
    // Trigger browser download
    const link = document.createElement('a')
    link.href = url
    link.download = r.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    // Refresh download count
    r.downloadCount++
  } catch {
    $q.notify({ type: 'negative', message: 'Error al descargar el archivo', position: 'bottom-right' })
  } finally {
    downloadingId.value = null
  }
}

watch(() => props.lessonId, load)
onMounted(load)
</script>
