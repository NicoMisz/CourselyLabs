<template>
  <q-layout view="lHh Lpr lFf" :class="$q.dark.isActive ? 'bg-grey-10' : 'bg-grey-1'">
    <q-header bordered :class="$q.dark.isActive ? 'bg-grey-9 text-white' : 'bg-white text-dark'">
      <q-toolbar>
        <q-btn flat dense icon="menu" @click="sidebarOpen = !sidebarOpen" class="lt-lg" />
        <q-btn flat dense icon="arrow_back" :to="`/cursos/${slug}`" />
        <q-toolbar-title class="text-body1 text-weight-medium ellipsis">
          {{ lesson?.title || 'Cargando...' }}
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <CourseNavSidebar
      v-model="sidebarOpen"
      :sections="sections"
      :course-slug="slug"
      :course-title="courseTitle"
      :active-lesson-id="lessonId"
      :completed-lesson-ids="courseProgress?.completedLessonIds || []"
      :progress-percent="courseProgress?.progressPercent || 0"
      :progress-text="progressText"
    />

    <q-page-container>
      <q-page class="column">
        <!-- Loading -->
        <div v-if="loading" class="col q-pa-lg">
          <q-skeleton type="rect" height="400px" class="q-mb-md" />
          <q-skeleton type="text" width="60%" />
          <q-skeleton type="text" width="40%" />
        </div>

        <!-- Error -->
        <div v-else-if="error" class="col q-pa-lg">
          <q-banner rounded class="bg-red-1 text-negative" inline-actions>
            {{ error }}
            <template #action>
              <q-btn flat color="negative" label="Reintentar" @click="loadData" />
            </template>
          </q-banner>
        </div>

        <!-- Content -->
        <template v-else-if="lesson">
          <!-- Resume video banner -->
          <q-banner
            v-if="showResumeBanner"
            class="bg-blue-1 q-mx-md q-mx-lg-lg q-mt-md"
            rounded
            inline-actions
            style="max-width: 960px; margin-left: auto; margin-right: auto; width: 100%"
          >
            ¿Continuar desde {{ formatTime(lessonProgress?.lastPositionSeconds || 0) }}?
            <template #action>
              <q-btn flat color="primary" label="Sí" @click="resumeVideo" />
              <q-btn flat color="grey-7" label="Empezar de nuevo" @click="showResumeBanner = false" />
            </template>
          </q-banner>

          <div class="col q-pa-md q-pa-lg-lg" style="max-width: 960px; margin: 0 auto; width: 100%">
            <h1 class="text-h5 q-mt-none q-mb-sm">{{ lesson.title }}</h1>
            <p v-if="lesson.description" class="text-body2 text-grey-7 q-mb-lg">{{ lesson.description }}</p>

            <!-- Block list (ordered) -->
            <div v-if="orderedBlocks.length > 0" class="block-stack">
              <div v-for="block in orderedBlocks" :key="block.id" class="block-section">
                <LessonVideoPlayer
                  v-if="block.type === 'video' && block.videoUrl"
                  :ref="block === firstVideoBlock ? (el => bindVideoRef(el)) : undefined"
                  :src="block.videoUrl"
                  @ended="handleVideoEnded"
                  @time-update="handleTimeUpdate"
                />
                <LessonTextViewer
                  v-else-if="block.type === 'text' && block.textContent"
                  :content="block.textContent"
                />
                <LessonPdfViewer
                  v-else-if="block.type === 'pdf' && block.pdfUrl"
                  :src="block.pdfUrl"
                />
                <AssessmentLessonView
                  v-else-if="isAssessmentBlockType(block.type)"
                  :block-id="block.id"
                />
                <div v-else class="text-grey-6 text-body2 q-pa-md">
                  Bloque sin contenido todavía.
                </div>
              </div>
            </div>

            <!-- Legacy fallback (lessons not yet split into blocks) -->
            <Transition v-else name="fade" mode="out-in">
              <LessonVideoPlayer
                v-if="lesson.type === 'video' && lesson.contentUrl"
                ref="videoPlayerRef"
                :key="lesson.id"
                :src="lesson.contentUrl"
                @ended="handleVideoEnded"
                @time-update="handleTimeUpdate"
              />
              <LessonTextViewer
                v-else-if="lesson.type === 'text' && lesson.contentText"
                :key="lesson.id"
                :content="lesson.contentText"
              />
              <LessonPdfViewer
                v-else-if="lesson.type === 'pdf' && lesson.contentUrl"
                :key="lesson.id"
                :src="lesson.contentUrl"
              />
              <div v-else :key="'empty'" class="q-pa-xl text-center text-grey-6">
                <q-icon name="info" size="48px" class="q-mb-md" />
                <div class="text-h6">Contenido no disponible</div>
              </div>
            </Transition>

            <div class="q-mt-lg row items-center justify-end">
              <q-btn
                v-if="!hasAssessmentBlock"
                :color="isCurrentCompleted ? 'positive' : 'grey-5'"
                :icon="isCurrentCompleted ? 'check_circle' : 'radio_button_unchecked'"
                :label="isCurrentCompleted ? 'Completada' : 'Marcar como completada'"
                :outline="!isCurrentCompleted"
                no-caps
                :loading="completingLesson"
                @click="handleToggleComplete"
              />
              <q-chip
                v-else-if="isCurrentCompleted"
                color="positive"
                text-color="white"
                icon="check_circle"
              >
                Completada
              </q-chip>
            </div>

            <LessonResources :lesson-id="lessonId" class="q-mt-lg" />
          </div>

          <LessonNavBar
            :prev="prevLesson"
            :next="nextLesson"
            :course-slug="slug"
          />
        </template>
      </q-page>
    </q-page-container>

    <!-- Course completion dialog -->
    <q-dialog v-model="showCompletionDialog">
      <q-card class="q-pa-lg text-center" style="min-width: 340px">
        <div class="completion-icon q-mx-auto q-mb-md">
          <q-icon name="emoji_events" size="48px" color="white" />
        </div>
        <div class="text-h5 q-mb-sm">¡Has completado el curso!</div>
        <div class="text-body2 text-grey-7 q-mb-lg">
          Felicidades por completar {{ courseTitle }}.
        </div>
        <q-card-actions align="center" class="q-gutter-sm">
          <q-btn flat color="primary" label="Explorar más cursos" to="/cursos" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCourseSections, getLessonById } from '../api/lesson'
import { getCourseBySlug } from '../api/course'
import { getCourseProgress, getLessonProgress, toggleLessonComplete, updateLessonPosition } from '../api/progress'
import type { Section, Lesson } from '../types/lesson'
import type { CourseProgress, LessonProgress } from '../types/progress'
import CourseNavSidebar from '../components/CourseNavSidebar.vue'
import LessonNavBar from '../components/LessonNavBar.vue'
import LessonVideoPlayer from '../components/LessonVideoPlayer.vue'
import LessonTextViewer from '../components/LessonTextViewer.vue'
import AssessmentLessonView from '../components/AssessmentLessonView.vue'
import { isAssessmentBlockType } from '../api/lessonBlock'

import LessonPdfViewer from '../components/LessonPdfViewer.vue'
import LessonResources from '../components/LessonResources.vue'

const route = useRoute()
const router = useRouter()

const slug = computed(() => String(route.params.slug || ''))
const lessonId = computed(() => String(route.params.lessonId || ''))

const loading = ref(true)
const error = ref('')
const sidebarOpen = ref(true)
const sections = ref<Section[]>([])
const lesson = ref<Lesson | null>(null)
const courseTitle = ref('')
const courseId = ref('')
const videoPlayerRef = ref<InstanceType<typeof LessonVideoPlayer> | null>(null)

// Progress state
const courseProgress = ref<CourseProgress | null>(null)
const lessonProgress = ref<LessonProgress | null>(null)
const completingLesson = ref(false)
const showCompletionDialog = ref(false)
const showResumeBanner = ref(false)

// Video position throttle
let positionTimer: ReturnType<typeof setInterval> | null = null
let lastSavedPosition = 0

const progressText = computed(() => {
  if (!courseProgress.value) return ''
  const { completedLessons, totalLessons, progressPercent } = courseProgress.value
  return `${completedLessons} de ${totalLessons} lecciones (${progressPercent}%)`
})

const isCurrentCompleted = computed(() =>
  courseProgress.value?.completedLessonIds.includes(lessonId.value) ?? false
)

const orderedBlocks = computed(() => {
  const list = lesson.value?.blocks || []
  return [...list].sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
})

const firstVideoBlock = computed(() =>
  orderedBlocks.value.find(b => b.type === 'video' && !!b.videoUrl) || null
)

const hasAssessmentBlock = computed(() =>
  orderedBlocks.value.some(b => isAssessmentBlockType(b.type))
)

function bindVideoRef(el: any) {
  videoPlayerRef.value = el
}

const allLessons = computed<Lesson[]>(() =>
  sections.value.flatMap(s => s.lessons)
)

const currentIndex = computed(() =>
  allLessons.value.findIndex(l => l.id === lessonId.value)
)

const prevLesson = computed(() =>
  currentIndex.value > 0 ? allLessons.value[currentIndex.value - 1] : null
)

const nextLesson = computed(() =>
  currentIndex.value >= 0 && currentIndex.value < allLessons.value.length - 1
    ? allLessons.value[currentIndex.value + 1]
    : null
)

function formatTime(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

function goToNext() {
  if (nextLesson.value) {
    router.push(`/cursos/${slug.value}/leccion/${nextLesson.value.id}`)
  }
}

async function handleToggleComplete() {
  completingLesson.value = true
  try {
    await toggleLessonComplete(lessonId.value)
    courseProgress.value = await getCourseProgress(courseId.value)

    if (courseProgress.value.progressPercent >= 100) {
      showCompletionDialog.value = true
    }
  } catch { /* silently fail */ }
  finally {
    completingLesson.value = false
  }
}

async function handleVideoEnded() {
  // Auto-complete on video end
  if (!isCurrentCompleted.value) {
    await handleToggleComplete()
  }
  goToNext()
}

function handleTimeUpdate(currentTime: number, duration: number) {
  // Auto-complete at 90% watched
  if (duration > 0 && currentTime / duration >= 0.9 && !isCurrentCompleted.value) {
    handleToggleComplete()
  }
}

function startPositionTracking() {
  stopPositionTracking()
  positionTimer = setInterval(async () => {
    if (!videoPlayerRef.value) return
    const current = videoPlayerRef.value.getCurrentTime?.() ?? 0
    const rounded = Math.floor(current)
    if (rounded > 0 && Math.abs(rounded - lastSavedPosition) >= 5) {
      lastSavedPosition = rounded
      await updateLessonPosition(lessonId.value, rounded).catch(() => {})
    }
  }, 10000)
}

function stopPositionTracking() {
  if (positionTimer) {
    clearInterval(positionTimer)
    positionTimer = null
  }
}

function resumeVideo() {
  showResumeBanner.value = false
  if (videoPlayerRef.value && lessonProgress.value) {
    videoPlayerRef.value.seekTo?.(lessonProgress.value.lastPositionSeconds)
  }
}

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const course = await getCourseBySlug(slug.value)
    courseTitle.value = course.title
    courseId.value = course.id
    sections.value = await getCourseSections(course.id)
    lesson.value = await getLessonById(lessonId.value)

    courseProgress.value = await getCourseProgress(course.id).catch(() => null)
    lessonProgress.value = await getLessonProgress(lessonId.value).catch(() => null)

    const hasVideo = orderedBlocks.value.some(b => b.type === 'video' && !!b.videoUrl)
      || (lesson.value?.type === 'video' && !!lesson.value.contentUrl)

    if (hasVideo && (lessonProgress.value?.lastPositionSeconds ?? 0) > 10) {
      showResumeBanner.value = true
    }
    if (hasVideo) {
      startPositionTracking()
    }
  } catch {
    error.value = 'No se pudo cargar la lección.'
  } finally {
    loading.value = false
  }
}

async function loadLesson() {
  stopPositionTracking()
  showResumeBanner.value = false
  try {
    lesson.value = await getLessonById(lessonId.value)
    lessonProgress.value = await getLessonProgress(lessonId.value).catch(() => null)

    const hasVideo = orderedBlocks.value.some(b => b.type === 'video' && !!b.videoUrl)
      || (lesson.value?.type === 'video' && !!lesson.value.contentUrl)
    if (hasVideo && (lessonProgress.value?.lastPositionSeconds ?? 0) > 10) {
      showResumeBanner.value = true
    }
    if (hasVideo) {
      lastSavedPosition = 0
      startPositionTracking()
    }
  } catch {
    error.value = 'No se pudo cargar la lección.'
  }
}

watch(lessonId, loadLesson)
onMounted(loadData)
onBeforeUnmount(stopPositionTracking)
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.completion-icon {
  width: 80px;
  height: 80px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f59e0b, var(--q-accent));
}

.block-stack {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.block-section {
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
  padding: 16px;
}
</style>
