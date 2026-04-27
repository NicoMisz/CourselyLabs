<template>
  <q-page class="grading-page">
    <div class="grading-header">
      <h1 class="page-title">Calificar entregas</h1>
      <p class="page-sub">
        Aqui puedes ver todas las entregas pendientes de calificar de tus cursos.
      </p>
    </div>

    <div class="filters-row">
      <q-select
        v-model="courseFilter"
        :options="courseOptions"
        label="Filtrar por curso"
        outlined
        emit-value
        map-options
        clearable
        dense
        style="min-width: 280px"
      />
      <q-space />
      <q-btn flat dense no-caps icon="refresh" label="Actualizar" @click="load" />
    </div>

    <div v-if="loading" class="text-center q-py-xl">
      <q-spinner-dots color="primary" size="40px" />
    </div>

    <q-banner v-else-if="error" rounded class="bg-red-1 text-negative">
      {{ error }}
    </q-banner>

    <div v-else-if="filteredSubmissions.length === 0" class="empty-state">
      <q-icon name="check_circle" size="48px" color="positive" />
      <div class="text-h6 q-mt-sm">Todo al dia</div>
      <p class="text-grey-7">No hay entregas pendientes de calificar.</p>
    </div>

    <q-list v-else bordered separator class="grading-list">
      <q-item
        v-for="s in filteredSubmissions"
        :key="s.id"
        clickable
        @click="openGrading(s)"
      >
        <q-item-section avatar>
          <q-icon
            :name="s.assessmentType === 'project' ? 'upload_file' : 'edit_note'"
            :color="s.assessmentType === 'project' ? 'accent' : 'deep-purple'"
            size="28px"
          />
        </q-item-section>
        <q-item-section>
          <q-item-label class="text-weight-medium">
            {{ s.studentName || 'Estudiante' }}
            <q-badge
              :color="s.assessmentType === 'project' ? 'accent' : 'deep-purple'"
              text-color="white"
              class="q-ml-sm"
            >
              {{ s.assessmentType === 'project' ? 'Proyecto' : 'Respuesta abierta' }}
            </q-badge>
          </q-item-label>
          <q-item-label caption>
            {{ s.courseTitle }} · {{ s.lessonTitle }}
          </q-item-label>
          <q-item-label caption>
            {{ formatDate(s.createdAt) }}
            <span v-if="s.fileName"> · {{ s.fileName }}</span>
          </q-item-label>
        </q-item-section>
        <q-item-section side>
          <q-icon name="chevron_right" />
        </q-item-section>
      </q-item>
    </q-list>

    <!-- Grading dialog -->
    <q-dialog v-model="gradingDialog" persistent>
      <q-card style="min-width: 520px; max-width: 90vw">
        <q-card-section>
          <div class="text-h6">Calificar entrega</div>
          <div class="text-caption text-grey-7 q-mt-xs">
            {{ current?.studentName }} · {{ current?.courseTitle }}
          </div>
          <div class="text-caption text-grey-6">{{ current?.lessonTitle }}</div>
        </q-card-section>

        <q-card-section>
          <div v-if="current?.assessmentType === 'project'" class="q-mb-md">
            <q-btn
              outline
              color="primary"
              icon="download"
              :label="current.fileName || 'Descargar archivo'"
              no-caps
              @click="download"
            />
          </div>
          <div v-else-if="current?.answerText" class="answer-box q-mb-md">
            {{ current.answerText }}
          </div>

          <q-input
            v-model.number="form.score"
            type="number"
            label="Puntuacion (0-100)"
            outlined
            min="0"
            max="100"
          />
          <q-input
            v-model="form.feedback"
            label="Feedback para el estudiante"
            outlined
            type="textarea"
            rows="4"
            class="q-mt-sm"
          />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn
            color="primary"
            unelevated
            label="Calificar"
            :loading="grading"
            :disable="form.score == null"
            @click="handleGrade"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { listPendingSubmissions, type PendingSubmission } from '@/api/grading'
import { gradeSubmission, getSubmissionDownloadUrl } from '@/api/assessment'

const $q = useQuasar()

const submissions = ref<PendingSubmission[]>([])
const loading = ref(true)
const error = ref('')

const courseFilter = ref<string | null>(null)

const courseOptions = computed(() => {
  const seen = new Map<string, string>()
  for (const s of submissions.value) {
    if (!seen.has(s.courseId)) seen.set(s.courseId, s.courseTitle)
  }
  return Array.from(seen.entries()).map(([id, title]) => ({ value: id, label: title }))
})

const filteredSubmissions = computed(() => {
  if (!courseFilter.value) return submissions.value
  return submissions.value.filter(s => s.courseId === courseFilter.value)
})

const gradingDialog = ref(false)
const current = ref<PendingSubmission | null>(null)
const form = reactive({ score: 70, feedback: '' })
const grading = ref(false)

function formatDate(d?: string | null) {
  if (!d) return ''
  return new Date(d).toLocaleString('es')
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    submissions.value = await listPendingSubmissions()
  } catch (err: any) {
    error.value = err?.response?.data?.message || 'No se pudieron cargar las entregas'
  } finally {
    loading.value = false
  }
}

function openGrading(s: PendingSubmission) {
  current.value = s
  form.score = 70
  form.feedback = ''
  gradingDialog.value = true
}

async function download() {
  if (!current.value) return
  try {
    const url = await getSubmissionDownloadUrl(current.value.id)
    window.open(url, '_blank')
  } catch {
    $q.notify({ type: 'negative', message: 'Error al descargar', position: 'bottom-right' })
  }
}

async function handleGrade() {
  if (!current.value) return
  grading.value = true
  try {
    await gradeSubmission(current.value.id, form.score, form.feedback)
    gradingDialog.value = false
    $q.notify({ type: 'positive', message: 'Entrega calificada', position: 'bottom-right' })
    await load()
  } catch {
    $q.notify({ type: 'negative', message: 'Error al calificar', position: 'bottom-right' })
  } finally {
    grading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.grading-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
}

.grading-header {
  margin-bottom: 16px;
}

.page-title {
  font-family: 'Monda', sans-serif;
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 4px;
  color: #0f172a;
}

.page-sub {
  color: #64748b;
  margin: 0;
}

.filters-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.empty-state {
  text-align: center;
  padding: 64px 24px;
  background: white;
  border: 2px dashed #e5e7eb;
  border-radius: 12px;
}

.grading-list {
  background: white;
  border-radius: 12px;
}

.answer-box {
  white-space: pre-wrap;
  padding: 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  max-height: 320px;
  overflow-y: auto;
  font-size: 0.9rem;
}
</style>
