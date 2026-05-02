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
        {{ t('assessment.editor.notConfiguredYet') }}
      </p>
      <q-btn
        color="primary"
        unelevated
        no-caps
        icon="add"
        :label="t('assessment.editor.createCta')"
        :loading="creating"
        @click="handleCreate"
      />
    </div>

    <!-- Existing assessment -->
    <template v-else>
      <!-- Settings -->
      <q-card flat bordered class="q-pa-md q-mb-md">
        <div class="text-caption text-weight-medium text-grey-7 q-mb-sm">
          {{ t('assessment.editor.title').toUpperCase() }}
        </div>

        <q-input
          v-model="form.description"
          :label="type === 'open_text' ? t('assessment.editor.descriptionTask') : t('assessment.editor.description')"
          outlined
          type="textarea"
          :rows="type === 'open_text' ? 5 : 3"
          dense
          class="q-mb-sm"
        />

        <div class="row q-col-gutter-sm">
          <div :class="type === 'quiz' ? 'col-12 col-sm-4' : 'col-12 col-sm-6'">
            <q-input
              v-model.number="form.maxAttempts"
              type="number"
              :label="type === 'quiz' ? t('assessment.editor.maxAttempts') : t('assessment.editor.maxResubmissions')"
              outlined dense min="1"
            />
          </div>
          <div v-if="type === 'quiz'" class="col-12 col-sm-4">
            <q-input
              v-model.number="form.timeLimitMinutes"
              type="number"
              :label="t('assessment.editor.timeLimit')"
              outlined dense min="0"
              :hint="t('assessment.editor.timeLimitHint')"
            />
          </div>
          <div :class="type === 'quiz' ? 'col-12 col-sm-4' : 'col-12 col-sm-6'">
            <q-input v-model.number="form.passingScore" type="number" :label="t('assessment.editor.passingScore')" outlined dense min="0" max="100" />
          </div>
        </div>

        <q-toggle
          v-if="type === 'quiz'"
          v-model="form.shuffleOptions"
          :label="t('assessment.editor.shuffleOptions')"
          class="q-mt-sm"
        />

        <q-banner v-if="type === 'open_text'" rounded class="bg-blue-1 text-blue-9 q-mt-sm" dense>
          <template #avatar><q-icon name="info" color="info" /></template>
          {{ t('assessment.editor.infoOpenText') }}
        </q-banner>

        <q-banner v-if="type === 'project'" rounded class="bg-blue-1 text-blue-9 q-mt-sm" dense>
          <template #avatar><q-icon name="info" color="info" /></template>
          {{ t('assessment.editor.infoProject') }}
        </q-banner>

        <div class="row justify-end q-mt-sm">
          <q-btn
            color="primary"
            unelevated
            no-caps
            :label="t('assessment.editor.saveConfig')"
            size="sm"
            :loading="savingConfig"
            @click="saveConfig"
          />
        </div>
      </q-card>

      <!-- Questions (only for quiz) -->
      <div v-if="type === 'quiz'">
        <div class="row items-center justify-between q-mb-sm">
          <div class="text-caption text-weight-medium text-grey-7">
            {{ t('assessment.editor.questionsCount', { count: questions.length }) }}
          </div>
        </div>

        <div v-if="questions.length === 0" class="empty-hint q-mb-md">
          {{ t('assessment.editor.questionsEmpty') }}
        </div>

        <div v-else class="q-mb-md">
          <q-card
            v-for="(q, qIdx) in questions"
            :key="q.id || `new-${qIdx}`"
            flat bordered
            :class="['question-card q-mb-sm', { 'is-new': !q.id }]"
          >
            <q-card-section>
              <div class="row items-center q-mb-sm">
                <div class="question-num">{{ qIdx + 1 }}</div>
                <q-input
                  v-model="q.questionText"
                  :placeholder="t('assessment.editor.questionPlaceholder')"
                  outlined
                  dense
                  type="textarea"
                  autogrow
                  class="col q-ml-sm"
                />
              </div>

              <div class="text-caption text-weight-medium text-grey-7 q-mt-sm q-mb-xs">
                {{ t('assessment.editor.options') }} <span class="text-grey-5">{{ t('assessment.editor.optionsHint') }}</span>
              </div>
              <div v-for="(opt, oIdx) in q.options" :key="oIdx" class="option-row q-mb-xs">
                <q-radio v-model="q.correctIdx" :val="oIdx" />
                <q-input
                  v-model="opt.optionText"
                  :placeholder="t('assessment.editor.optionPlaceholder', { n: oIdx + 1 })"
                  outlined dense
                  class="col"
                />
                <q-btn flat dense round icon="close" color="grey-7" size="sm" @click="q.options.splice(oIdx, 1)">
                  <q-tooltip>{{ t('assessment.editor.removeOption') }}</q-tooltip>
                </q-btn>
              </div>
              <q-btn flat dense no-caps icon="add" :label="t('assessment.editor.addOption')" color="primary" size="sm" @click="addOption(q)" />

              <!-- Advanced -->
              <q-expansion-item
                :label="t('assessment.editor.advanced')"
                dense
                header-class="text-caption text-grey-7"
                class="q-mt-md"
              >
                <q-input v-model.number="q.points" type="number" :label="t('assessment.editor.points')" outlined dense min="1" style="max-width: 200px" class="q-mb-sm" />
                <div class="text-caption text-grey-7 q-mb-xs">{{ t('assessment.editor.explanationLabel') }}</div>
                <div v-for="(opt, oIdx) in q.options" :key="`exp-${oIdx}`" class="row items-center q-gutter-sm q-mb-xs">
                  <span class="text-caption text-grey-7" style="min-width: 70px">{{ t('assessment.editor.optionPlaceholder', { n: oIdx + 1 }) }}</span>
                  <q-input v-model="opt.explanation" :placeholder="opt.optionText || t('assessment.editor.optionPlaceholder', { n: oIdx + 1 })" outlined dense class="col" />
                </div>
              </q-expansion-item>

              <div class="row justify-between q-mt-md">
                <q-btn flat dense no-caps icon="delete" color="negative" :label="t('assessment.editor.removeQuestion')" size="sm" @click="removeQuestion(q, qIdx)" />
                <q-btn color="primary" unelevated no-caps :label="t('assessment.editor.saveQuestion')" size="sm" :loading="q.saving" @click="saveQuestion(q)" />
              </div>
            </q-card-section>
          </q-card>
        </div>

        <q-btn
          color="primary"
          unelevated
          no-caps
          icon="add"
          :label="t('assessment.editor.addQuestion')"
          class="full-width q-mt-md"
          @click="addQuestion"
        />
      </div>

      <!-- Pending submissions live in Calificar; the editor stays focused on authoring. -->
      <q-banner
        v-if="type === 'project' || type === 'open_text'"
        rounded
        class="bg-blue-1 text-blue-9 q-mt-sm"
        dense
      >
        <template #avatar><q-icon name="info" color="info" /></template>
        {{ t('assessment.editor.gradingHint') }}
        <router-link to="/instructor/calificar" class="text-weight-medium">{{ t('nav.grading') }}</router-link>.
      </q-banner>

      <!-- Danger zone -->
      <div class="row justify-end q-mt-md">
        <q-btn
          flat
          dense
          no-caps
          icon="delete"
          color="negative"
          size="sm"
          :label="t('assessment.editor.deleteAssessment')"
          @click="confirmDeleteAssessment"
        />
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
      {{ t('assessment.editor.freeLimitBanner') }}
      <template #action>
        <q-btn flat dense color="amber-8" :label="t('nav.premium')" no-caps to="/premium" />
      </template>
    </q-banner>

    <!-- Confirm delete assessment -->
    <q-dialog v-model="deleteAssessmentDialog">
      <q-card style="min-width: 380px">
        <q-card-section>
          <div class="text-h6">{{ t('assessment.editor.deleteConfirmTitle') }}</div>
        </q-card-section>
        <q-card-section>
          {{ t('assessment.editor.deleteConfirmBody') }}
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('common.cancel')" v-close-popup />
          <q-btn color="negative" unelevated :label="t('common.delete')" :loading="deletingAssessment" @click="handleDeleteAssessment" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { useI18n } from 'vue-i18n'
import {
  getAssessmentForEditByBlock,
  createAssessmentForBlock,
  updateAssessment,
  deleteAssessment,
  addQuestion as apiAddQuestion,
  updateQuestion as apiUpdateQuestion,
  deleteQuestion as apiDeleteQuestion,
} from '../api/assessment'
import type { Assessment } from '../types/assessment'

const props = defineProps<{
  blockId: string
  type: string
  canUsePremiumFeatures: boolean
}>()

const emit = defineEmits<{
  (e: 'created', assessment: Assessment): void
}>()

const $q = useQuasar()
const { t } = useI18n()

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

const deleteAssessmentDialog = ref(false)
const deletingAssessment = ref(false)

function iconForType(typeStr: string) {
  switch (typeStr) {
    case 'quiz': return 'quiz'
    case 'project': return 'upload_file'
    case 'open_text': return 'edit_note'
    default: return 'assignment'
  }
}

function colorForType(typeStr: string) {
  switch (typeStr) {
    case 'quiz': return 'primary'
    case 'project': return 'accent'
    case 'open_text': return 'deep-purple'
    default: return 'grey-7'
  }
}

function titleForType(typeStr: string) {
  switch (typeStr) {
    case 'quiz': return t('assessment.type.quiz')
    case 'project': return t('assessment.type.project')
    case 'open_text': return t('assessment.type.openText')
    default: return t('assessment.editor.title')
  }
}

async function load() {
  loading.value = true
  try {
    assessment.value = await getAssessmentForEditByBlock(props.blockId)
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
    }
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  creating.value = true
  try {
    assessment.value = await createAssessmentForBlock(props.blockId, {
      description: '',
      maxAttempts: 3,
      passingScore: 70,
      shuffleOptions: true,
    })
    emit('created', assessment.value)
    $q.notify({ type: 'positive', message: t('assessment.editor.createdOk'), position: 'bottom-right' })
    await load()
  } catch (err: any) {
    $q.notify({
      type: 'negative',
      message: err?.response?.data?.message || t('assessment.editor.createError'),
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
    $q.notify({ type: 'positive', message: t('assessment.editor.configSaved'), position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: t('assessment.editor.configSaveError'), position: 'bottom-right' })
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
    $q.notify({ type: 'positive', message: t('assessment.editor.questionSaved'), position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: t('assessment.editor.questionSaveError'), position: 'bottom-right' })
  } finally {
    q.saving = false
  }
}

async function removeQuestion(q: QuestionEdit, idx: number) {
  if (q.id) {
    try {
      await apiDeleteQuestion(q.id)
    } catch {
      $q.notify({ type: 'negative', message: t('assessment.editor.errorRemove'), position: 'bottom-right' })
      return
    }
  }
  questions.value.splice(idx, 1)
}

function confirmDeleteAssessment() {
  deleteAssessmentDialog.value = true
}

async function handleDeleteAssessment() {
  if (!assessment.value) return
  deletingAssessment.value = true
  try {
    await deleteAssessment(assessment.value.id)
    assessment.value = null
    questions.value = []
    deleteAssessmentDialog.value = false
    $q.notify({ type: 'positive', message: t('assessment.editor.deletedOk'), position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: t('assessment.editor.deleteError'), position: 'bottom-right' })
  } finally {
    deletingAssessment.value = false
  }
}

watch(() => props.blockId, load)
watch(() => props.type, load)
onMounted(load)
</script>

<style scoped>
.assessment-editor {
  margin-top: 12px;
}

.empty-create {
  padding: 24px;
  background: var(--app-bg-soft);
  border-radius: 8px;
  text-align: center;
}

.empty-hint {
  padding: 16px;
  background: var(--app-bg-soft);
  border-radius: 8px;
  color: var(--app-text-soft);
  font-size: 0.875rem;
  text-align: center;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-card {
  border-radius: 10px;
  transition: border-color 0.15s ease;
}

.question-card.is-new {
  border-color: var(--q-primary);
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.08);
}

.question-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--q-primary);
  color: white;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.answer-box {
  white-space: pre-wrap;
  padding: 12px;
  background: var(--app-bg-soft);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
  font-size: 0.875rem;
}
</style>
