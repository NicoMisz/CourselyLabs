<template>
  <div>
    <div
      class="uploader-zone"
      :class="{ 'uploader-zone--drag': isDragging, 'uploader-zone--disabled': disabled || uploading }"
      @click="triggerInput"
      @dragenter.prevent="isDragging = true"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
    >
      <input
        ref="fileInput"
        type="file"
        :accept="accept"
        class="hidden-input"
        @change="handleInput"
      />

      <div v-if="uploading" class="text-center">
        <q-linear-progress :value="progress / 100" color="primary" size="8px" rounded class="q-mb-sm" />
        <div class="text-caption text-grey-7">Subiendo... {{ progress }}%</div>
      </div>

      <div v-else class="text-center">
        <q-icon :name="icon" size="36px" :color="isDragging ? 'primary' : 'grey-6'" />
        <div class="text-body2 q-mt-sm">
          <span class="text-weight-medium">{{ label }}</span>
          <div class="text-caption text-grey-6">{{ hint }}</div>
        </div>
      </div>
    </div>

    <div v-if="error" class="text-caption text-negative q-mt-xs">{{ error }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{
  accept?: string
  maxSizeMb?: number
  label?: string
  hint?: string
  icon?: string
  disabled?: boolean
}>(), {
  accept: '*',
  maxSizeMb: 100,
  label: 'Click o arrastra un archivo',
  hint: '',
  icon: 'cloud_upload',
  disabled: false,
})

const emit = defineEmits<{
  upload: [file: File, onProgress: (pct: number) => void]
  error: [message: string]
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)
const uploading = ref(false)
const progress = ref(0)
const error = ref('')

function triggerInput() {
  if (props.disabled || uploading.value) return
  fileInput.value?.click()
}

function handleInput(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    processFile(target.files[0])
  }
  target.value = ''
}

function handleDrop(e: DragEvent) {
  isDragging.value = false
  if (props.disabled || uploading.value) return
  if (e.dataTransfer?.files && e.dataTransfer.files.length > 0) {
    processFile(e.dataTransfer.files[0])
  }
}

function processFile(file: File) {
  error.value = ''
  if (file.size > props.maxSizeMb * 1024 * 1024) {
    error.value = `El archivo supera el limite de ${props.maxSizeMb} MB`
    emit('error', error.value)
    return
  }

  uploading.value = true
  progress.value = 0
  emit('upload', file, (pct: number) => {
    progress.value = pct
  })
}

function finish(errMsg?: string) {
  uploading.value = false
  progress.value = 0
  if (errMsg) error.value = errMsg
}

defineExpose({ finish })
</script>

<style scoped>
.uploader-zone {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 1.5rem;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.15s ease;
}

.uploader-zone:hover {
  border-color: #0f766e;
  background: #f0fdfa;
}

.uploader-zone--drag {
  border-color: #0f766e;
  background: #f0fdfa;
}

.uploader-zone--disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.hidden-input {
  display: none;
}
</style>
