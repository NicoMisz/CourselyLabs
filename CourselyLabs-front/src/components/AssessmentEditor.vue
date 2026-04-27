<template>
  <div class="assessment-editor">
    <q-separator class="q-my-md" />

    <div class="row items-center q-mb-sm">
      <q-icon :name="iconForType(type)" :color="colorForType(type)" size="20px" class="q-mr-sm" />
      <div class="text-subtitle2">{{ titleForType(type) }}</div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center q-pa-md">
      <q-spinner-dots color="primary" size="32px" />
    </div>

    <!-- Not yet created -->
    <div v-else-if="!assessment" class="empty-create">
      <p class="text-body2 text-grey-7 q-mb-sm">
        Esta leccion aun no tiene una evaluacion configurada.
      </p>
      <q-btn
        color="primary"
        unelevated
        no-caps
        icon="add"
        label="Configurar evaluacion"
        :loading="creating"
        @click="handleCreate"
      />
    </div>

    <!-- Existing assessment -->
    <template v-else>
      <!-- Settings -->
      <q-card flat bordered class="q-pa-md q-mb-md">
        <div class="text-caption text-weight-medium text-grey-7 q-mb-sm">CONFIGURACION</div>

        <q-input
          v-model="form.description"
          label="Descripcion / Instrucciones"
          outlined
          type="textarea"
          rows="3"
          dense
          class="q-mb-sm"
        />

        <div class="row q-col-gutter-sm">
          <div class="col-12 col-sm-4">
            <q-input v-model.number="form.maxAttempts" type="number" label="Intentos maximos" outlined dense min="1" />
          </div>
          <div class="col-12 col-sm-4">
            <q-input v-model.number="form.timeLimitMinutes" type="number" label="Tiempo limite (min)" outlined dense min="0" hint="Vacio = sin limite" />
          </div>
          <div class="col-12 col-sm-4">
            <q-input v-model.number="form.passingScore" type="number" label="% para aprobar" outlined dense min="0" max="100" />
          </div>
        </div>

        <q-toggle
          v-if="type === 'quiz'"
          v-model="form.shuffleOptions"
          label="Aleatorizar orden de opciones"
          class="q-mt-sm"
        />

        <div class="row justify-end q-mt-sm">
          <q-btn
            color="primary"
            unelevated
            no-caps
            label="Guardar configuracion"
            size="sm"
            :loading="savingConfig"
            @click="saveConfig"
          />
        </div>
      </q-card>

      <!-- Questions (only for quiz) -->
      <div v-if="type === 'quiz'">
        <div class="row items-center justify-between q-mb-sm">
          <div class="text-caption text-weight-medium text-grey-7">PREGUNTAS</div>
          <q-btn outline color="primary" no-caps icon="add" label="Añadir pregunta" size="sm" @click="addQuestion" />
        </div>

        <div v-if="questions.length === 0" class="empty-hint">
          Aun no hay preguntas. Añade al menos una.
        </div>

        <q-list v-else bordered separator class="q-mb-md">
          <q-expansion-item
            v-for="(q, qIdx) in questions"
            :key="q.id"
            :label="`${qIdx + 1}. ${q.questionText || '(sin texto)'}`"
            header-class="text-weight-medium"
          >
            <q-card flat>
              <q-card-section>
                <q-input
                  v-model="q.questionText"
                  label="Texto de la pregunta"
                  outlined
                  dense
                  type="textarea"
                  rows="2"
                  class="q-mb-sm"
                />
                <q-input v-model.number="q.points" type="number" label="Puntos" outlined dense min="1" style="max-width: 200px" />

                <div class="text-caption text-weight-medium text-grey-7 q-mt-md q-mb-xs">OPCIONES</div>
                <div v-for="(opt, oIdx) in q.options" :key="oIdx" class="option-row q-mb-xs">
                  <q-radio v-model="q.correctIdx" :val="oIdx" />
                  <q-input v-model="opt.optionText" placeholder="Texto de la opcion" outlined dense class="col" />
                  <q-input v-model="opt.explanation" placeholder="Explicacion (opcional)" outlined dense class="col" />
                  <q-btn flat dense round icon="close" color="negative" size="sm" @click="q.options.splice(oIdx, 1)" />
                </div>
                <q-btn flat dense no-caps icon="add" label="Añadir opcion" color="primary" size="sm" @click="addOption(q)" />

                <div class="row justify-between q-mt-md">
                  <q-btn flat dense no-caps icon="delete" color="negative" label="Eliminar pregunta" size="sm" @click="removeQuestion(q, qIdx)" />
                  <q-btn color="primary" unelevated no-caps label="Guardar pregunta" size="sm" :loading="q.saving" @click="saveQuestion(q)" />
                </div>
              </q-card-section>
            </q-card>
          </q-expansion-item>
        </q-list>
      </div>

      <!-- Submissions panel (for project / open_text) -->
      <div v-if="type === 'project' || type === 'open_text'">
        <div class="row items-center justify-between q-mb-sm">
          <div class="text-caption text-weight-medium text-grey-7">ENTREGAS PENDIENTES</div>
          <q-btn flat dense no-caps icon="refresh" size="sm" @click="loadPending" />
        </div>

        <div v-if="pendingSubmissions.length === 0" class="empty-hint">
          No hay entregas pendientes de calificar.
        </div>

        <q-list v-else bordered separator class="q-mb-md">
          <q-item
            v-for="s in pendingSubmissions"
            :key="s.id"
            clickable
            @click="openGrading(s)"
          >
            <q-item-section avatar>
              <q-icon :name="s.type === 'project' ? 'upload_file' : 'edit_note'" color="primary" />
            </q-item-section>
            <q-item-section>
              <q-item-label>{{ s.studentName || 'Estudiante' }}</q-item-label>
              <q-item-label caption>
                {{ formatDate(s.createdAt) }}
                <span v-if="s.fileName"> · {{ s.fileName }}</span>
              </q-item-label>
            </q-item-section>
            <q-item-section side><q-icon name="chevron_right" /></q-item-section>
          </q-item>
        </q-list>
      </div>
    </template>

    <!-- Premium lock if non-premium -->
    <q-banner
      v-if="!canUsePremiumFeatures && !assessment"
      rounded
      class="bg-amber-1 q-mt-sm"
      dense
    >
      <template #avatar>
        <q-icon name="workspace_premium" color="amber-8" />
      </template>
      Plan gratuito: limite de 2 evaluaciones de cada tipo por curso.
      <template #action>
        <q-btn flat dense color="amber-8" label="Hazte Premium" no-caps to="/premium" />
      </template>
    </q-banner>

    <!-- Grading dialog -->
    <q-dialog v-model="gradingDialog" persistent>
      <q-card style="min-width: 500px; max-width: 90vw">
        <q-card-section>
          <div class="text-h6">Calificar entrega</div>
          <div class="text-body2 text-grey-7 q-mt-xs">
            {{ currentSubmission?.studentName }}
          </div>
        </q-card-section>

        <q-card-section>
          <!-- Project: download link -->
          <div v-if="currentSubmission?.type === 'project'" class="q-mb-md">
            <q-btn
              outline
              color="primary"
              icon="download"
              :label="currentSubmission.fileName || 'Descargar archivo'"
              no-caps
              @click="downloadSubmission"
            />
          </div>

          <!-- Open text: show answer -->
          <div v-else-if="currentSubmission?.answerText" class="answer-box q-mb-md">
            {{ currentSubmission.answerText }}
          </div>

          <q-input
            v-model.number="gradeForm.score"
            type="number"
            label="Puntuacion (0-100)"
            outlined
            min="0"
            max="100"
          />
          <q-input
            v-model="gradeForm.feedback"
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
            :loading="gradingLoading"
            :disable="gradeForm.score == null"
            @click="handleGrade"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import {
  getAssessmentForEdit,
  createAssessment,
  updateAssessment,
  addQuestion as apiAddQuestion,
  updateQuestion as apiUpdateQuestion,
  deleteQuestion as apiDeleteQuestion,
  getPendingSubmissions,
  getSubmissionDownloadUrl,
  gradeSubmission,
} from '../api/assessment'
import type { Assessment, Submission } from '../types/assessment'

const props = defineProps<{
  lessonId: string
  type: string
  canUsePremiumFeatures: boolean
}>()

const $q = useQuasar()

const loading = ref(true)
const creating = ref(false)
const savingConfig = ref(false)

const assessment = ref<Assessment | null>(null)
const form = reactive({
  description: '',
  maxAttempts: 3,
  timeLimitMinutes: null as number | null,
  passingScore: 70,
  shuffleOptions: true,
})

interface QuestionEdit {
  id: string
  questionText: string
  points: number
  options: { id?: string; optionText: string; isCorrect: boolean; explanation?: string }[]
  correctIdx: number
  saving: boolean
}

const questions = ref<QuestionEdit[]>([])

const pendingSubmissions = ref<Submission[]>([])
const gradingDialog = ref(false)
const currentSubmission = ref<Submission | null>(null)
const gradeForm = reactive({ score: 0, feedback: '' })
const gradingLoading = ref(false)

function iconForType(t: string) {
  switch (t) {
    case 'quiz': return 'quiz'
    case 'project': return 'upload_file'
    case 'open_text': return 'edit_note'
    default: return 'assignment'
  }
}

function colorForType(t: string) {
  switch (t) {
    case 'quiz': return 'primary'
    case 'project': return 'accent'
    case 'open_text': return 'deep-purple'
    default: return 'grey-7'
  }
}

function titleForType(t: string) {
  switch (t) {
    case 'quiz': return 'Cuestionario'
    case 'project': return 'Entrega de proyecto'
    case 'open_text': return 'Respuesta abierta'
    default: return 'Evaluacion'
  }
}

function formatDate(d?: string | null) {
  if (!d) return ''
  return new Date(d).toLocaleString('es')
}

async function load() {
  loading.value = true
  try {
    assessment.value = await getAssessmentForEdit(props.lessonId)
    if (assessment.value) {
      form.description = assessment.value.description || ''
      form.maxAttempts = assessment.value.maxAttempts
      form.timeLimitMinutes = assessment.value.timeLimitMinutes ?? null
      form.passingScore = assessment.value.passingScore
      form.shuffleOptions = assessment.value.shuffleOptions
      questions.value = (assessment.value.questions || []).map(q => ({
        id: q.id,
        questionText: q.questionText,
        points: q.points || 1,
        options: (q.options || []).map(o => ({
          id: o.id,
          optionText: o.optionText,
          isCorrect: o.isCorrect || false,
          explanation: o.explanation || '',
        })),
        correctIdx: (q.options || []).findIndex(o => o.isCorrect),
        saving: false,
      }))
      if (props.type !== 'quiz') {
        await loadPending()
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  creating.value = true
  try {
    assessment.value = await createAssessment(props.lessonId, {
      type: props.type as any,
      description: '',
      maxAttempts: 3,
      passingScore: 70,
      shuffleOptions: true,
    })
    $q.notify({ type: 'positive', message: 'Evaluacion creada', position: 'bottom-right' })
    await load()
  } catch (err: any) {
    $q.notify({
      type: 'negative',
      message: err?.response?.data?.message || 'Error al crear',
      position: 'bottom-right',
    })
  } finally {
    creating.value = false
  }
}

async function saveConfig() {
  if (!assessment.value) return
  savingConfig.value = true
  try {
    await updateAssessment(assessment.value.id, {
      description: form.description,
      maxAttempts: form.maxAttempts,
      timeLimitMinutes: form.timeLimitMinutes,
      passingScore: form.passingScore,
      shuffleOptions: form.shuffleOptions,
    })
    $q.notify({ type: 'positive', message: 'Configuracion guardada', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al guardar', position: 'bottom-right' })
  } finally {
    savingConfig.value = false
  }
}

function addQuestion() {
  if (!assessment.value) return
  questions.value.push({
    id: '',
    questionText: '',
    points: 1,
    options: [
      { optionText: '', isCorrect: true },
      { optionText: '', isCorrect: false },
    ],
    correctIdx: 0,
    saving: false,
  })
}

function addOption(q: QuestionEdit) {
  q.options.push({ optionText: '', isCorrect: false })
}

async function saveQuestion(q: QuestionEdit) {
  if (!assessment.value) return
  q.saving = true
  try {
    const payload = {
      questionText: q.questionText,
      points: q.points,
      position: questions.value.indexOf(q),
      options: q.options.map((o, i) => ({
        id: o.id,
        optionText: o.optionText,
        isCorrect: i === q.correctIdx,
        explanation: o.explanation,
        position: i,
      })) as any,
    }
    if (!q.id) {
      const saved = await apiAddQuestion(assessment.value.id, payload)
      q.id = saved.id
    } else {
      await apiUpdateQuestion(q.id, payload)
    }
    $q.notify({ type: 'positive', message: 'Pregunta guardada', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al guardar pregunta', position: 'bottom-right' })
  } finally {
    q.saving = false
  }
}

async function removeQuestion(q: QuestionEdit, idx: number) {
  if (q.id) {
    try {
      await apiDeleteQuestion(q.id)
    } catch {
      $q.notify({ type: 'negative', message: 'Error al eliminar', position: 'bottom-right' })
      return
    }
  }
  questions.value.splice(idx, 1)
}

async function loadPending() {
  if (!assessment.value) return
  try {
    pendingSubmissions.value = await getPendingSubmissions(assessment.value.id)
  } catch {
    pendingSubmissions.value = []
  }
}

function openGrading(s: Submission) {
  currentSubmission.value = s
  gradeForm.score = 70
  gradeForm.feedback = ''
  gradingDialog.value = true
}

async function downloadSubmission() {
  if (!currentSubmission.value) return
  try {
    const url = await getSubmissionDownloadUrl(currentSubmission.value.id)
    window.open(url, '_blank')
  } catch {
    $q.notify({ type: 'negative', message: 'Error al descargar', position: 'bottom-right' })
  }
}

async function handleGrade() {
  if (!currentSubmission.value) return
  gradingLoading.value = true
  try {
    await gradeSubmission(currentSubmission.value.id, gradeForm.score, gradeForm.feedback)
    gradingDialog.value = false
    $q.notify({ type: 'positive', message: 'Entrega calificada', position: 'bottom-right' })
    await loadPending()
  } catch {
    $q.notify({ type: 'negative', message: 'Error al calificar', position: 'bottom-right' })
  } finally {
    gradingLoading.value = false
  }
}

watch(() => props.lessonId, load)
watch(() => props.type, load)
onMounted(load)
</script>

<style scoped>
.assessment-editor {
  margin-top: 12px;
}

.empty-create {
  padding: 24px;
  background: #f9fafb;
  border-radius: 8px;
  text-align: center;
}

.empty-hint {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 0.875rem;
  text-align: center;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.answer-box {
  white-space: pre-wrap;
  padding: 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
  font-size: 0.875rem;
}
</style>
