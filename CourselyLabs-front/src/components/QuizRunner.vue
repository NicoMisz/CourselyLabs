<template>
  <div class="quiz-runner">
    <!-- Top bar with timer + nav -->
    <div class="quiz-topbar">
      <div class="text-subtitle1 text-weight-medium">
        Pregunta {{ currentIndex + 1 }} de {{ questions.length }}
      </div>

      <div v-if="assessment.timeLimitMinutes" class="timer" :class="timerClass">
        <q-icon name="timer" /> {{ timeLeftLabel }}
      </div>

      <q-btn flat dense no-caps icon="close" label="Salir" @click="confirmCancel" />
    </div>

    <!-- Question nav panel -->
    <div class="nav-panel">
      <q-btn
        v-for="(q, i) in questions"
        :key="q.id"
        :outline="i !== currentIndex"
        :color="navColor(i)"
        :label="String(i + 1)"
        size="sm"
        round
        @click="currentIndex = i"
      />
    </div>

    <!-- Current question -->
    <q-card v-if="currentQuestion" flat bordered class="question-card">
      <q-card-section>
        <div class="text-caption text-grey-7 q-mb-xs">
          {{ currentQuestion.points }} punto{{ currentQuestion.points === 1 ? '' : 's' }}
        </div>
        <div class="text-h6 q-mb-md">{{ currentQuestion.questionText }}</div>

        <q-option-group
          v-model="selectedByQuestion[currentQuestion.id]"
          :options="optionList"
          color="primary"
          type="radio"
        />

        <q-btn
          flat
          dense
          no-caps
          :icon="isMarked ? 'flag' : 'outlined_flag'"
          :color="isMarked ? 'warning' : 'grey-7'"
          :label="isMarked ? 'Marcada' : 'Marcar para revisar'"
          class="q-mt-md"
          @click="toggleMark(currentQuestion.id)"
        />
      </q-card-section>
    </q-card>

    <!-- Bottom nav -->
    <div class="bottom-bar">
      <q-btn flat no-caps icon="arrow_back" label="Anterior" :disable="currentIndex === 0" @click="prev" />
      <q-btn
        v-if="currentIndex < questions.length - 1"
        color="primary"
        unelevated
        no-caps
        icon-right="arrow_forward"
        label="Siguiente"
        @click="next"
      />
      <q-btn
        v-else
        color="positive"
        unelevated
        no-caps
        icon="check"
        label="Enviar"
        :loading="submitting"
        @click="confirmSubmit"
      />
    </div>

    <!-- Submit confirm -->
    <q-dialog v-model="showSubmitDialog">
      <q-card style="min-width: 380px">
        <q-card-section>
          <div class="text-h6">Enviar examen</div>
        </q-card-section>
        <q-card-section>
          <div class="row items-center q-gutter-sm q-mb-sm">
            <q-icon name="check_circle" color="positive" /> Respondidas: {{ answeredCount }}
          </div>
          <div v-if="unansweredCount > 0" class="row items-center q-gutter-sm q-mb-sm text-warning">
            <q-icon name="warning" /> Sin responder: {{ unansweredCount }}
          </div>
          <div v-if="markedCount > 0" class="row items-center q-gutter-sm">
            <q-icon name="flag" color="warning" /> Marcadas: {{ markedCount }}
          </div>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn color="positive" label="Enviar definitivamente" :loading="submitting" @click="submitNow" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useQuasar } from 'quasar'
import { submitQuiz } from '../api/assessment'
import type { Assessment, AssessmentResult, QuizAnswer } from '../types/assessment'

const props = defineProps<{
  assessment: Assessment
  attemptId: string
}>()

const emit = defineEmits<{
  submitted: [result: AssessmentResult]
  cancel: []
}>()

const $q = useQuasar()
const questions = computed(() => props.assessment.questions || [])
const currentIndex = ref(0)
const submitting = ref(false)
const showSubmitDialog = ref(false)

const selectedByQuestion = reactive<Record<string, string | null>>({})
const markedSet = reactive<Set<string>>(new Set<string>())

const currentQuestion = computed(() => questions.value[currentIndex.value])

const optionList = computed(() =>
  (currentQuestion.value?.options || []).map(o => ({ label: o.optionText, value: o.id }))
)

const isMarked = computed(() => !!currentQuestion.value?.id && markedSet.has(currentQuestion.value.id))

function navColor(i: number) {
  const q = questions.value[i]
  if (!q) return 'grey'
  if (markedSet.has(q.id)) return 'warning'
  if (selectedByQuestion[q.id]) return 'positive'
  return 'grey'
}

function toggleMark(qId: string) {
  if (markedSet.has(qId)) markedSet.delete(qId)
  else markedSet.add(qId)
}

function prev() { if (currentIndex.value > 0) currentIndex.value-- }
function next() { if (currentIndex.value < questions.value.length - 1) currentIndex.value++ }

const answeredCount = computed(() =>
  Object.values(selectedByQuestion).filter(v => !!v).length
)
const unansweredCount = computed(() => questions.value.length - answeredCount.value)
const markedCount = computed(() => markedSet.size)

function confirmSubmit() {
  showSubmitDialog.value = true
}

function confirmCancel() {
  $q.dialog({
    title: '¿Salir del examen?',
    message: 'Tu progreso no se guardara y consumiras un intento.',
    ok: { label: 'Salir', color: 'negative', flat: true, noCaps: true },
    cancel: { label: 'Continuar', noCaps: true },
  }).onOk(() => emit('cancel'))
}

async function submitNow() {
  submitting.value = true
  try {
    const answers: QuizAnswer[] = questions.value.map(q => ({
      questionId: q.id,
      selectedOptionId: selectedByQuestion[q.id] || null,
    }))
    const result = await submitQuiz(props.attemptId, answers)
    showSubmitDialog.value = false
    emit('submitted', result)
  } catch (err: any) {
    $q.notify({
      type: 'negative',
      message: err?.response?.data?.message || 'Error al enviar',
      position: 'bottom-right',
    })
  } finally {
    submitting.value = false
  }
}

// Timer
const startTime = Date.now()
const timeLeftSeconds = ref(props.assessment.timeLimitMinutes ? props.assessment.timeLimitMinutes * 60 : 0)
let timerInterval: ReturnType<typeof setInterval> | null = null

const timeLeftLabel = computed(() => {
  const s = Math.max(0, timeLeftSeconds.value)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
})

const timerClass = computed(() => {
  if (timeLeftSeconds.value <= 60) return 'timer-critical'
  if (timeLeftSeconds.value <= 300) return 'timer-warning'
  return ''
})

onMounted(() => {
  if (props.assessment.timeLimitMinutes) {
    timerInterval = setInterval(() => {
      const elapsed = Math.floor((Date.now() - startTime) / 1000)
      timeLeftSeconds.value = Math.max(0, props.assessment.timeLimitMinutes! * 60 - elapsed)
      if (timeLeftSeconds.value === 0) {
        if (timerInterval) clearInterval(timerInterval)
        autoSubmit()
      }
    }, 1000)
  }
})

onBeforeUnmount(() => {
  if (timerInterval) clearInterval(timerInterval)
})

async function autoSubmit() {
  $q.notify({
    type: 'warning',
    message: 'Tiempo agotado. Enviando automáticamente...',
    position: 'top',
    timeout: 3000,
  })
  await submitNow()
}
</script>

<style scoped>
.quiz-runner {
  max-width: 720px;
  margin: 0 auto;
}

.quiz-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  margin-bottom: 12px;
}

.timer {
  font-family: monospace;
  font-size: 1.1rem;
  font-weight: 700;
  padding: 6px 12px;
  background: var(--app-primary-soft);
  border: 1px solid var(--app-primary-tint);
  border-radius: 6px;
  color: var(--q-primary);
}

.timer-warning {
  background: var(--app-warning-soft);
  border-color: #fcd34d;
  color: var(--app-warning-text);
}

.timer-critical {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #991b1b;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.nav-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 12px;
  background: var(--app-bg-soft);
  border-radius: 8px;
  margin-bottom: 16px;
}

.question-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.bottom-bar {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
}
</style>
