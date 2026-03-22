<template>
  <q-layout view="lHh Lpr lFf">
    <q-header bordered class="bg-white text-dark">
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
          <div class="col q-pa-md q-pa-lg-lg" style="max-width: 960px; margin: 0 auto; width: 100%">
            <Transition name="fade" mode="out-in">
              <LessonVideoPlayer
                v-if="lesson.type === 'video' && lesson.contentUrl"
                :key="lesson.id"
                :src="lesson.contentUrl"
                @ended="goToNext"
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

            <div class="q-mt-lg">
              <h1 class="text-h5 q-my-sm">{{ lesson.title }}</h1>
              <p v-if="lesson.description" class="text-body2 text-grey-7">{{ lesson.description }}</p>
            </div>
          </div>

          <LessonNavBar
            :prev="prevLesson"
            :next="nextLesson"
            :course-slug="slug"
          />
        </template>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCourseSections, getLessonById } from '../api/lesson'
import { getCourseBySlug } from '../api/course'
import type { Section, Lesson } from '../types/lesson'
import CourseNavSidebar from '../components/CourseNavSidebar.vue'
import LessonNavBar from '../components/LessonNavBar.vue'
import LessonVideoPlayer from '../components/LessonVideoPlayer.vue'
import LessonTextViewer from '../components/LessonTextViewer.vue'
import LessonPdfViewer from '../components/LessonPdfViewer.vue'

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

function goToNext() {
  if (nextLesson.value) {
    router.push(`/cursos/${slug.value}/leccion/${nextLesson.value.id}`)
  }
}

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const course = await getCourseBySlug(slug.value)
    courseTitle.value = course.title
    sections.value = await getCourseSections(course.id)
    lesson.value = await getLessonById(lessonId.value)
  } catch {
    error.value = 'No se pudo cargar la leccion.'
  } finally {
    loading.value = false
  }
}

async function loadLesson() {
  try {
    lesson.value = await getLessonById(lessonId.value)
  } catch {
    error.value = 'No se pudo cargar la leccion.'
  }
}

watch(lessonId, loadLesson)
onMounted(loadData)
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
</style>
