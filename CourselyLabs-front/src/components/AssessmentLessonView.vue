<template>
  <div class="assessment-wrap">
    <!-- Loading -->
    <div v-if="loading" class="text-center q-pa-xl">
      <q-spinner-dots color="primary" size="40px" />
    </div>

    <!-- No assessment configured -->
    <div v-else-if="!assessment" class="empty">
      <q-icon name="assignment" size="48px" color="grey-4" />
      <div class="text-h6 text-grey-6 q-mt-sm">Esta evaluación aún no está configurada</div>
    </div>

    <!-- Intro screen -->
    <q-card v-else-if="screen === 'intro'" flat bordered class="intro-card">
      <q-card-section class="text-center">
        <q-icon :name="iconForType(assessment.type)" size="48px" :color="colorForType(assessment.type)" class="q-mb-sm" />
        <h2 class="text-h5 q-my-sm">{{ titleForType(assessment.type) }}</h2>
      </q-card-section>

      <q-card-section v-if="assessment.description" class="rich-content" v-html="assessment.description" />

      <q-card-section class="row q-col-gutter-md justify-center">
        <div v-if="assessment.type === 'quiz' && assessment.questions" class="info-pill">
          <q-icon name="quiz" /> {{ assessment.questions.length }} pregunta{{ assessment.questions.length !== 1 ? 's' : '' }}
        </div>
        <div v-if="assessment.timeLimitMinutes" class="info-pill">
          <q-icon name="timer" /> {{ assessment.timeLimitMinutes }} min
        </div>
        <div class="info-pill">
          <q-icon name="trending_up" /> {{ assessment.passingScore }}% para aprobar
        </div>
        <div class="info-pill" :class="{ 'pill-warning': attemptsLeft <= 1 }">
          <q-icon name="loop" /> {{ attemptsLeft }} de {{ assessment.maxAttempts }} intentos
        </div>
      </q-card-section>

      <!-- Past attempts -->
      <q-card-section v-if="attempts.length > 0">
        <div class="text-subtitle2 q-mb-sm">Tus intentos:</div>
        <q-list dense bordered>
          <q-item v-for="a in attempts" :key="a.id" clickable @click="viewResult(a)">
            <q-item-section avatar>
              <q-icon
                :name="a.passed ? 'check_circle' : (a.status === 'submitted' ? 'hourglass_top' : 'cancel')"
                :color="a.passed ? 'positive' : (a.status === 'submitted' ? 'warning' : 'negative')"
              />
            </q-item-section>
            <q-item-section>
              <q-item-label>Intento {{ a.attemptNumber }}</q-item-label>
              <q-item-label caption>
                {{ formatDate(a.submittedAt || a.startedAt) }}
                <span v-if="a.score != null"> · {{ a.score }}%</span>
                <span v-if="a.status === 'submitted'"> · Pendiente de calificar</span>
              </q-item-label>
            </q-item-section>
            <q-item-section side><q-icon name="chevron_right" /></q-item-section>
          </q-item>
        </q-list>
      </q-card-section>

      <q-card-actions align="center" class="q-pa-md">
        <q-btn
          v-if="attemptsLeft > 0 && !hasOpenAttempt"
          color="primary"
          unelevated
          no-caps
          icon="play_arrow"
          :label="attempts.length === 0 ? 'Comenzar' : 'Nuevo intento'"
          size="md"
          :loading="starting"
          @click="handleStart"
        />
        <q-banner v-else-if="hasOpenAttempt" rounded class="bg-warning text-white">
          Tienes una entrega pendiente de calificar.
        </q-banner>
        <q-banner v-else rounded class="bg-grey-3 text-grey-8">
          Has agotado todos los intentos.
        </q-banner>
      </q-card-actions>
    </q-card>

    <!-- Quiz in progress -->
    <QuizRunner
      v-else-if="screen === 'quiz' && currentAssessment"
      :assessment="currentAssessment"
      :attempt-id="currentAttemptId!"
      @submitted="handleQuizSubmitted"
      @cancel="cancelAttempt"
    />

    <!-- Project upload -->
    <ProjectSubmitView
      v-else-if="screen === 'project'"
      :attempt-id="currentAttemptId!"
      :passing-score="assessment.passingScore"
      @submitted="handleSubmitted"
    />

    <!-- Open text -->
    <OpenTextSubmitView
      v-else-if="screen === 'open_text'"
      :attempt-id="currentAttemptId!"
      @submitted="handleSubmitted"
    />

    <!-- Results -->
    <AssessmentResultsView
      v-else-if="screen === 'results' && currentResult"
      :result="currentResult"
      :assessment-type="assessment.type"
      @close="reset"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useQuasar } from 'quasar'
import {
  getAssessmentByBlock,
  getMyAttempts,
  startAttempt,
  getAttemptResult,
} from '../api/assessment'
import type { Assessment, AssessmentAttempt, AssessmentResult } from '../types/assessment'
import QuizRunner from './QuizRunner.vue'
import ProjectSubmitView from './ProjectSubmitView.vue'
import OpenTextSubmitView from './OpenTextSubmitView.vue'
import AssessmentResultsView from './AssessmentResultsView.vue'

type Screen = 'intro' | 'quiz' | 'project' | 'open_text' | 'results'

const props = defineProps<{ blockId: string }>()

const $q = useQuasar()
const loading = ref(true)
const assessment = ref<Assessment | null>(null)
const attempts = ref<AssessmentAttempt[]>([])
const screen = ref<Screen>('intro')
const currentAttemptId = ref<string | null>(null)
const currentAssessment = ref<Assessment | null>(null)
const currentResult = ref<AssessmentResult | null>(null)
const starting = ref(false)

const attemptsLeft = computed(() => {
  if (!assessment.value) return 0
  return Math.max(0, assessment.value.maxAttempts - attempts.value.length)
})

const hasOpenAttempt = computed(() =>
  attempts.value.some(a => a.status === 'submitted')
)

function iconForType(type: string) {
  switch (type) {
    case 'quiz': return 'quiz'
    case 'project': return 'upload_file'
    case 'open_text': return 'edit_note'
    default: return 'assignment'
  }
}

function colorForType(type: string) {
  switch (type) {
    case 'quiz': return 'primary'
    case 'project': return 'accent'
    case 'open_text': return 'deep-purple'
    default: return 'grey-7'
  }
}

function titleForType(type: string) {
  switch (type) {
    case 'quiz': return 'Cuestionario'
    case 'project': return 'Entrega de proyecto'
    case 'open_text': return 'Respuesta abierta'
    default: return 'Evaluación'
  }
}

function formatDate(d?: string | null) {
  if (!d) return ''
  return new Date(d).toLocaleString('es')
}

async function loadAll() {
  loading.value = true
  reset()
  try {
    assessment.value = await getAssessmentByBlock(props.blockId)
    if (assessment.value) {
      attempts.value = await getMyAttempts(assessment.value.id)
    }
  } catch {
    assessment.value = null
  } finally {
    loading.value = false
  }
}

async function handleStart() {
  if (!assessment.value) return
  starting.value = true
  try {
    const started = await startAttempt(assessment.value.id)
    currentAttemptId.value = started.attemptId
    currentAssessment.value = started.assessment
    if (assessment.value.type === 'quiz') screen.value = 'quiz'
    else if (assessment.value.type === 'project') screen.value = 'project'
    else screen.value = 'open_text'
  } catch (err: any) {
    $q.notify({
      type: 'negative',
      message: err?.response?.data?.message || 'Error al iniciar',
      position: 'bottom-right',
    })
  } finally {
    starting.value = false
  }
}

function handleQuizSubmitted(result: AssessmentResult) {
  currentResult.value = result
  screen.value = 'results'
  // Reload attempts so the count updates
  if (assessment.value) {
    getMyAttempts(assessment.value.id).then(a => { attempts.value = a }).catch(() => {})
  }
}

function handleSubmitted() {
  $q.notify({ type: 'positive', message: 'Entrega enviada. Te avisaremos cuando se califique.', position: 'bottom-right' })
  loadAll()
}

async function viewResult(a: AssessmentAttempt) {
  if (a.status === 'in_progress') return
  try {
    currentResult.value = await getAttemptResult(a.id)
    screen.value = 'results'
  } catch (err: any) {
    $q.notify({ type: 'negative', message: 'No se pudo cargar el resultado', position: 'bottom-right' })
  }
}

function cancelAttempt() {
  reset()
  if (assessment.value) {
    getMyAttempts(assessment.value.id).then(a => { attempts.value = a }).catch(() => {})
  }
}

function reset() {
  screen.value = 'intro'
  currentAttemptId.value = null
  currentAssessment.value = null
  currentResult.value = null
}

watch(() => props.blockId, loadAll)
onMounted(loadAll)
</script>

<style scoped>
.assessment-wrap {
  max-width: 800px;
  margin: 0 auto;
}

.empty {
  text-align: center;
  padding: 64px 24px;
}

.intro-card {
  border-radius: 12px;
}

.info-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f1f5f9;
  border-radius: 999px;
  font-size: 0.875rem;
  color: #475569;
}

.info-pill.pill-warning {
  background: #fef3c7;
  color: #92400e;
}
</style>
