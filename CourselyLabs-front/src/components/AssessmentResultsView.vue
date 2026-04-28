<template>
  <q-card flat bordered class="results-card">
    <!-- Header with score -->
    <q-card-section :class="['results-header', result.passed ? 'pass' : 'fail']">
      <q-icon
        :name="result.passed ? 'celebration' : 'sentiment_dissatisfied'"
        size="48px"
        color="white"
      />
      <h2 class="text-h4 q-my-sm" style="font-family: Monda, sans-serif">
        {{ result.passed ? '¡Aprobado!' : 'No aprobado' }}
      </h2>
      <div v-if="result.score != null" class="text-h2 q-my-sm" style="font-family: Monda, sans-serif">
        {{ result.score }}<span class="text-h5">%</span>
      </div>
      <div class="text-caption">
        Puntuación mínima para aprobar: {{ result.passingScore }}%
      </div>
    </q-card-section>

    <!-- Quiz: per-question breakdown -->
    <q-card-section v-if="assessmentType === 'quiz'">
      <div class="text-subtitle1 q-mb-md">Detalle de respuestas</div>
      <q-list separator bordered>
        <q-expansion-item
          v-for="(q, qIdx) in result.questions"
          :key="q.id"
          :label="`${qIdx + 1}. ${q.questionText}`"
          :icon="isCorrect(q.id) ? 'check_circle' : 'cancel'"
          :header-class="isCorrect(q.id) ? 'text-positive' : 'text-negative'"
        >
          <q-card flat>
            <q-card-section>
              <div v-for="opt in q.options" :key="opt.id" class="option-row">
                <q-icon
                  :name="iconForOption(q.id, opt.id, opt.isCorrect)"
                  :color="colorForOption(q.id, opt.id, opt.isCorrect)"
                  size="20px"
                />
                <span :class="{ 'text-weight-medium': opt.isCorrect }">
                  {{ opt.optionText }}
                </span>
                <span v-if="isSelected(q.id, opt.id)" class="text-caption text-grey-7"> · Tu respuesta</span>
              </div>
              <div v-if="explanationForCorrect(q)" class="explanation">
                <q-icon name="lightbulb" color="amber-8" /> {{ explanationForCorrect(q) }}
              </div>
            </q-card-section>
          </q-card>
        </q-expansion-item>
      </q-list>
    </q-card-section>

    <!-- Project / open_text: pending grading -->
    <q-card-section v-else-if="result.score == null" class="text-center text-grey-7">
      <q-icon name="hourglass_top" size="36px" color="warning" />
      <div class="text-body1 q-mt-sm">Pendiente de calificación por el instructor.</div>
    </q-card-section>

    <q-card-actions align="center" class="q-pa-md">
      <q-btn flat no-caps label="Volver" @click="$emit('close')" />
    </q-card-actions>
  </q-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AssessmentResult, QuizQuestion } from '../types/assessment'

const props = defineProps<{
  result: AssessmentResult
  assessmentType: string
}>()

defineEmits<{ close: [] }>()

const selectedByQuestion = computed(() => {
  const map = new Map<string, string | null | undefined>()
  for (const a of props.result.answers) {
    map.set(a.questionId, a.selectedOptionId)
  }
  return map
})

function isSelected(qId: string, oId: string): boolean {
  return selectedByQuestion.value.get(qId) === oId
}

function isCorrect(qId: string): boolean {
  const selectedId = selectedByQuestion.value.get(qId)
  if (!selectedId) return false
  const q = props.result.questions.find(x => x.id === qId)
  const opt = q?.options?.find(o => o.id === selectedId)
  return Boolean(opt?.isCorrect)
}

function iconForOption(qId: string, oId: string, optCorrect: boolean | undefined): string {
  if (optCorrect) return 'check_circle'
  if (isSelected(qId, oId)) return 'cancel'
  return 'radio_button_unchecked'
}

function colorForOption(qId: string, oId: string, optCorrect: boolean | undefined): string {
  if (optCorrect) return 'positive'
  if (isSelected(qId, oId)) return 'negative'
  return 'grey-5'
}

function explanationForCorrect(q: QuizQuestion): string | undefined {
  return q.options?.find(o => o.isCorrect)?.explanation
}
</script>

<style scoped>
.results-card {
  border-radius: 12px;
  max-width: 800px;
  margin: 0 auto;
  overflow: hidden;
}

.results-header {
  text-align: center;
  padding: 32px 24px;
  color: white;
}

.results-header.pass {
  background: linear-gradient(135deg, #059669, #10b981);
}

.results-header.fail {
  background: linear-gradient(135deg, #6b7280, #94a3b8);
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
}

.explanation {
  margin-top: 12px;
  padding: 10px 14px;
  background: #fffbeb;
  border-left: 4px solid #d97706;
  border-radius: 0 8px 8px 0;
  font-size: 0.875rem;
  color: #78350f;
}
</style>
