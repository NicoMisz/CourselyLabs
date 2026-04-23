<template>
  <q-page class="q-pa-md q-pa-lg-md">
  <div class="content-wrap">

    <!-- Loading -->
    <template v-if="loading">
      <q-skeleton type="rect" height="250px" class="q-mb-md" />
      <q-skeleton type="text" width="60%" />
      <q-skeleton type="text" width="90%" class="q-mb-lg" />
      <q-skeleton type="rect" height="300px" />
    </template>

    <!-- No trobat -->
    <template v-else-if="notFound">
      <q-card flat bordered class="q-pa-lg text-center">
        <div class="text-h5 q-mb-sm">Curso no encontrado</div>
        <p class="text-grey-7">El curso que buscas no existe o ya no esta disponible.</p>
        <q-btn color="primary" label="Explorar cursos" to="/cursos" />
      </q-card>
    </template>

    <!-- Error general -->
    <template v-else-if="errorMessage">
      <q-banner rounded class="bg-red-1 text-negative q-mb-md" inline-actions>
        {{ errorMessage }}
        <template #action>
          <q-btn flat color="negative" label="Reintentar" @click="fetchCourse" />
        </template>
      </q-banner>
    </template>

    <!-- Contingut -->
    <template v-else-if="course">

      <!-- Error d'inscripció -->
      <q-banner v-if="enrollError" rounded class="bg-red-1 text-negative q-mb-md" inline-actions>
        {{ enrollError }}
        <template #action>
          <q-btn flat color="negative" label="Cerrar" @click="enrollError = ''" />
        </template>
      </q-banner>

      <CourseBreadcrumb :title="course.title" :category-name="course.categoryName" />

      <div class="row q-col-gutter-lg">

        <!-- Columna esquerra -->
        <div class="col-12 col-md-8">
          <CourseHero
            :title="course.title"
            :short-description="course.shortDescription"
            :level="course.level"
            :is-free="course.isFree"
            :price="course.price"
            :thumbnail-url="course.thumbnailUrl"
            :students-count="course.studentsCount"
            :updated-at="course.updatedAt"
          />

          <q-tabs
            v-model="tab"
            class="q-mt-lg"
            dense
            align="left"
            active-color="primary"
            indicator-color="primary"
          >
            <q-tab name="descripcion" label="Descripcion" />
            <q-tab name="prerequisitos" label="Relacionados" />
            <q-tab name="contenido" label="Contenido" />
            <q-tab name="instructores" label="Instructores" />
            <q-tab name="valoraciones" label="Valoraciones" />
          </q-tabs>

          <q-separator />

          <q-tab-panels v-model="tab" animated>
            <q-tab-panel name="descripcion">
              <div v-if="course.description" class="rich-content" v-html="course.description" />
              <p v-else class="text-grey-7">Sin descripcion completa por ahora.</p>
            </q-tab-panel>

            <q-tab-panel name="prerequisitos">
              <div class="column q-gutter-md">
                <CoursePrerequisitesTab
                  :statuses="prerequisiteStatuses"
                  :required-by="requiredBy"
                  :loading="prerequisiteStatusInitialLoading"
                />
              </div>
            </q-tab-panel>

            <q-tab-panel name="contenido">
              <CourseSectionList
                v-if="course.sections?.length"
                :sections="course.sections"
                :course-slug="course.slug"
                :enrolled="enrolled"
              />
              <q-banner v-else class="bg-grey-2 text-grey-8" rounded>
                Este curso aun no tiene contenido publicado.
              </q-banner>
            </q-tab-panel>

            <q-tab-panel name="instructores">
              <div v-if="course.instructors?.length" class="row q-col-gutter-md">
                <div v-for="i in course.instructors" :key="i.id" class="col-12 col-sm-6">
                  <q-card flat bordered class="q-pa-md">
                    <div class="text-subtitle1 text-weight-medium">{{ i.name }}</div>
                    <div class="text-body2 text-grey-7">{{ i.bio || 'Sin bio' }}</div>
                  </q-card>
                </div>
              </div>
              <q-banner v-else class="bg-grey-2 text-grey-8" rounded>
                No hay instructores asignados.
              </q-banner>
            </q-tab-panel>

            <q-tab-panel name="valoraciones">
              <q-banner class="bg-grey-2 text-grey-8" rounded>
                <CourseTabReviews
                  v-if="course"
                  :course-id="course.id"
                  :enrolled="enrolled"
                  :is-logged-in="authStore.isLoggedIn"
                  :completed-lessons="courseCompletedLessons"
                  :current-user-id="authStore.user?.id"
                />
              </q-banner>
            </q-tab-panel>
          </q-tab-panels>
        </div>

        <!-- Columna dreta -->
        <div class="col-12 col-md-4">
          <CourseSidebar
              class="q-mt-md"
              :course-id="course.id"
              :course-slug="course.slug"
              :level="course.level"
              :duration-text="course.durationText"
              :students-count="course.studentsCount"
              :average-rating="course.averageRating"
              :is-free="course.isFree"
              :price="course.price"
              :enrolled="enrolled"
              :loading="enrollLoading"
              :prerequisite-blockers="prerequisiteBlockers" 
              @enroll="handleEnroll"
              @continue="handleContinueCourse"
              @open-related-tab="tab = 'prerequisitos'"
          />

          <!-- Banner de bloqueo si no cumple prerequisitos
              :prerequisite-blockers="prerequisiteBlockers"  -->
        </div>

      </div>
    </template>

    <EnrollSuccessDialog
      v-model="showEnrollSuccess"
      @close="showEnrollSuccess = false"
      @go-to-course="showEnrollSuccess = false"
    />

  </div>
</q-page>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import CourseHero from '../components/CourseHero.vue';
import CourseSidebar from '../components/CourseSidebar.vue';
import CourseBreadcrumb from '../components/CourseBreadcrumb.vue';
import EnrollSuccessDialog from '../components/EnrollSuccessDialog.vue';
import CourseSectionList from '../components/CourseSectionList.vue';
import { getCourseBySlug, getCourseInstructors } from '../api/course';
import { checkEnrollment, createEnrollment } from '../api/enrollment';
import type { CourseDetail } from '../types/course';

import CourseTabReviews from '@/components/CourseTabReviews.vue';
import { getCourseProgress } from '@/api/progress';

import CoursePrerequisitesTab from '@/components/CoursePrerequisitesTab.vue'
import { getCoursePrerequisites, getCoursePrerequisiteBlockers, getCoursePrerequisiteStatus } from '@/api/prerequisite'
import type { CoursePrerequisite, BlockedPrerequisite, CoursePrerequisiteStatus } from '@/types/prerequisite'

// Nuevo: endpoint para cursos relacionados (reemplaza "Relacionados" por "Prerequisitos" y muestra progreso y bloqueo)
import { getCourseRelated } from '@/api/prerequisite'
import type { RelatedCourseItem } from '@/types/prerequisite'

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const tab = ref('descripcion');
const loading = ref(true);
const notFound = ref(false);
const errorMessage = ref('');
const course = ref<CourseDetail | null>(null);
const enrolled = ref(false);
const enrollLoading = ref(false);
const showEnrollSuccess = ref(false);
const enrollError = ref('');

const courseCompletedLessons = ref(0);

const prerequisites = ref<CoursePrerequisite[]>([])
const prerequisiteBlockers = ref<BlockedPrerequisite[]>([])
const prerequisiteStatuses = ref<CoursePrerequisiteStatus[]>([])
const prerequisiteStatusInitialLoading = ref(false)
const prerequisiteStatusRefreshing = ref(false)
let prerequisitesPollTimer: ReturnType<typeof setInterval> | null = null

const requiredBy = ref<RelatedCourseItem[]>([]) // Nuevo: cursos que requieren este curso como prerequisito

function setOgMeta(name: string, content: string) {
  const selector = `meta[property="${name}"]`
  let meta = document.querySelector(selector) as HTMLMetaElement | null
  if (!meta) {
    meta = document.createElement('meta')
    meta.setAttribute('property', name)
    document.head.appendChild(meta)
  }
  meta.setAttribute('content', content)
}

function updateSeo(c: CourseDetail) {
  document.title = `${c.title} | CourselyLabs`
  setOgMeta('og:title', c.title)
  setOgMeta('og:description', c.shortDescription || c.description || 'Curso en CourselyLabs')
  setOgMeta('og:image', c.thumbnailUrl || '')
}

async function fetchCourse() {
  loading.value = true;
  notFound.value = false;
  errorMessage.value = '';
  try {
    const slug = String(route.params.slug || '');
    const data = await getCourseBySlug(slug);
    if (!data || !data.id) {
      notFound.value = true
      course.value = null
      return
    }
    const instructors = await getCourseInstructors(data.id).catch(() => []);
    course.value = { ...data, instructors };
    updateSeo(course.value);
  } catch (error: unknown) {
    const maybeStatus = (error as { response?: { status?: number } })?.response?.status
    if (maybeStatus === 404) {
      notFound.value = true
    } else {
      errorMessage.value = 'No se pudo cargar el curso. Intenta nuevamente.'
    }
  } finally {
    loading.value = false
  }
}

async function fetchEnrollmentState(courseId?: string) {
  if (!authStore.isLoggedIn || !courseId) {
    enrolled.value = false;
    return;
  }
  enrolled.value = await checkEnrollment(courseId).catch(() => false);
}

async function handleEnroll() {
  if (!course.value?.id) return;
  enrollError.value = '';
  enrollLoading.value = true;
  try {
    await createEnrollment({ courseId: course.value.id });
    enrolled.value = true;
    showEnrollSuccess.value = true;
  } catch (error: unknown) {
    const message = (error as { response?: { data?: { message?: string } } })
      ?.response?.data?.message;
    enrollError.value = message || 'No se pudo completar la inscripcion. Intenta nuevamente.';
  } finally {
    enrollLoading.value = false;
  }
}

async function fetchCourseProgress(courseId?: string) {
  if (!authStore.isLoggedIn || !enrolled.value || !courseId) {
    courseCompletedLessons.value = 0
    return
  }

  try {
    const p = await getCourseProgress(courseId);
    courseCompletedLessons.value = p.completedLessons ?? 0;
  } catch {
    courseCompletedLessons.value = 0;
  }
}

async function fetchPrerequisites(courseId?: string) {
	if (!courseId) {
		prerequisites.value = []
		prerequisiteBlockers.value = []
		return
	}

	if (course.value?.isFree) {
		prerequisites.value = []
		prerequisiteBlockers.value = []
		return
	}

	// 1) Prioridad: lo que ya venga en el detalle del curso
	prerequisites.value = Array.isArray(course.value?.prerequisites)
		? course.value!.prerequisites
		: []

	// 2) Fallback: si viene vacio, pedir endpoint dedicado
	if (!prerequisites.value.length) {
		try {
			prerequisites.value = await getCoursePrerequisites(courseId)
		} catch (err) {
			console.warn('[prerequisites] error loading list:', err)
			prerequisites.value = []
		}
	}

	// Blockers para banner y bloqueo de boton
	try {
		if (authStore.isLoggedIn) {
			prerequisiteBlockers.value = await getCoursePrerequisiteBlockers(courseId)
		} else {
			prerequisiteBlockers.value = []
		}
	} catch (err) {
		console.warn('[prerequisites] error loading blockers:', err)
		prerequisiteBlockers.value = []
	}
}

// Nuevo: cargar estado de prerequisitos para mostrar progreso y bloqueo
async function fetchPrerequisiteStatus(courseId?: string, silent = false) {
  if (!courseId || course.value?.isFree) {
    prerequisiteStatuses.value = []
    return
  }

  if (silent) {
    prerequisiteStatusRefreshing.value = true
  } else {
    prerequisiteStatusInitialLoading.value = true
  }

  try {
    const data = await getCoursePrerequisiteStatus(courseId)
    if (data.length || !silent) {
      prerequisiteStatuses.value = data
    }
  } catch {
    if (!prerequisiteStatuses.value.length) {
      prerequisiteStatuses.value = []
    }
  } finally {
    prerequisiteStatusInitialLoading.value = false
    prerequisiteStatusRefreshing.value = false
  }
}

// Nuevo: cargar cursos relacionados (reemplaza "Relacionados" por "Prerequisitos" y 
// muestra progreso y bloqueo)
async function fetchRelatedAuth(courseId?: string) {
  if (!courseId ) {
    requiredBy.value = []
    return
  }

  try {
    const data = await getCourseRelated(courseId)
    requiredBy.value = data.requiredBy ?? []
  } catch {
    requiredBy.value = []
  }
}

// Nuevo: iniciar polling de estado de prerequisitos cada 15s cuando se vea la pestaña de prerequisitos
function startPrerequisitesPolling() {
  stopPrerequisitesPolling()
  prerequisitesPollTimer = setInterval(() => {
    if (tab.value === 'prerequisitos' && course.value?.id) {
      fetchPrerequisiteStatus(course.value.id, true)
    }
  }, 15000)
}

function stopPrerequisitesPolling() {
    if (prerequisitesPollTimer) {
        clearInterval(prerequisitesPollTimer)
        prerequisitesPollTimer = null
    }
}

function handleContinueCourse() {
  router.push(route.fullPath);
}

watch(() => course.value?.id, fetchEnrollmentState, { immediate: true })

watch(() => route.params.slug, fetchCourse)
onMounted(fetchCourse);

// Nuevo: cargar progreso cuando cambia el curso
watch(
  () => course.value?.id,
  (id) => {
    if (id) fetchCourseProgress(id)
  },
  { immediate: true }
);

// Nuevo: recargar progreso si cambia la inscripción
watch(enrolled, (isEnrolled) => {
  if (isEnrolled && course.value?.id) {
    fetchCourseProgress(course.value.id)
  }
});

watch(
    () => course.value?.id,
    (id) => {
        if (!id) return
        fetchPrerequisites(id)
        fetchPrerequisiteStatus(id)
    },
    { immediate: true }
)

// Nuevo: cargar cursos relacionados (reemplaza "Relacionados" por "Prerequisitos" y muestra progreso y bloqueo)
watch(
  () => course.value?.id,
  (id) => {
    if (!id) return
    fetchRelatedAuth(id)
  },
  { immediate: true }
)

watch(tab, (newTab) => {
    if (newTab === 'prerequisitos' && course.value?.id) {
        fetchPrerequisiteStatus(course.value.id)
    }
})

watch(
    () => authStore.isLoggedIn,
    () => {
        if (course.value?.id) {
            fetchPrerequisites(course.value.id)
            fetchPrerequisiteStatus(course.value.id)
        }
    }
)

// Nuevo: recargar cursos relacionados (reemplaza "Relacionados" por "Prerequisitos" y muestra progreso y bloqueo) si cambia el estado de login
watch(
  () => authStore.isLoggedIn,
  () => {
    if (!course.value?.id) return
    fetchRelatedAuth(course.value.id)
  }
)

onMounted(() => {
    startPrerequisitesPolling()
})

onBeforeUnmount(() => {
    stopPrerequisitesPolling()
})

</script>
