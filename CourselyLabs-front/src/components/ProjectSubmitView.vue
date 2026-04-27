<template>
  <q-card flat bordered class="submit-card">
    <q-card-section>
      <div class="text-h6 q-mb-sm">
        <q-icon name="upload_file" color="accent" /> Entrega de proyecto
      </div>
      <p class="text-body2 text-grey-7">
        Sube el archivo de tu entrega. Una vez enviado, se quedara pendiente de revision por el instructor.
      </p>
    </q-card-section>

    <q-card-section>
      <FileUploader
        ref="uploaderRef"
        :max-size-mb="50"
        label="Sube tu entrega"
        hint="PDF, DOCX, ZIP, imagen... hasta 50 MB"
        icon="upload_file"
        button-label="Elegir archivo"
        @upload="handleUpload"
      />
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useQuasar } from 'quasar'
import FileUploader from './FileUploader.vue'
import { submitProject } from '../api/assessment'

const props = defineProps<{
  attemptId: string
  passingScore: number
}>()

// Suppress unused warning for prop kept for future banner
void props

const emit = defineEmits<{ submitted: [] }>()

const $q = useQuasar()
const uploaderRef = ref<InstanceType<typeof FileUploader> | null>(null)

async function handleUpload(file: File, onProgress: (pct: number) => void) {
  try {
    await submitProject(props.attemptId, file, onProgress)
    uploaderRef.value?.finish()
    emit('submitted')
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Error al enviar'
    uploaderRef.value?.finish(msg)
    $q.notify({ type: 'negative', message: msg, position: 'bottom-right' })
  }
}
</script>

<style scoped>
.submit-card {
  border-radius: 12px;
  max-width: 700px;
  margin: 0 auto;
}
</style>
