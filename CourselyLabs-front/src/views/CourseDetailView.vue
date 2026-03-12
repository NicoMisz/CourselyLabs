<template>
  <q-page class="q-pa-md q-pa-lg-md">
    <div class="content-wrap">
      <template v-if="loading">
        <q-skeleton type="rect" height="250px" class="q-mb-md" />
        <q-skeleton type="text" width="60%" />
        <q-skeleton type="text" width="90%" class="q-mb-lg" />
        <q-skeleton type="rect" height="300px" />
      </template>

      <template v-else-if="notFound">
        <q-card flat bordered class="q-pa-lg text-center">
          <div class="text-h5 q-mb-sm">Curso no encontrado</div>
          <p class="text-grey-7">El curso que buscas no existe o ya no esta disponible.</p>
          <q-btn color="primary" label="Explorar cursos" to="/cursos" />
        </q-card>
      </template>

      <template v-else-if="errorMessage">
        <q-banner rounded class="bg-red-1 text-negative q-mb-md" inline-actions>
          <template #avatar>
            <q-icon name="warning" />
          </template>
          {{ errorMessage }}
          <template #action>
            <q-btn flat color="negative" label="Reintentar" @click="fetchCourse" />
          </template>
        </q-banner>
      </template>

      <template v-else-if="course">
        <CourseBreadcrumb :title="course.title" :category-name="course.categoryName" />

        <div class="row q-col-gutter-lg">
          <div class="col-12 col-md-8">
            <CourseHero
              :title="course.title"
              :short-description="course.shortDescription"
              :level="course.level"
              :free="course.free"
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
              <q-tab name="contenido" label="Contenido" />
              <q-tab name="instructores" label="Instructores" />
              <q-tab name="valoraciones" label="Valoraciones" />
            </q-tabs>

            <q-separator />

            <q-tab-panels v-model="tab" animated>
              <q-tab-panel name="descripcion">
                <p>{{ course.description || 'Sin descripcion completa por ahora.' }}</p>
              </q-tab-panel>

              <q-tab-panel name="contenido">
                <q-banner class="bg-blue-1 text-info" rounded>
                  Contenido del curso disponible proximamente.
                </q-banner>
              </q-tab-panel>

              <q-tab-panel name="instructores">
                <div v-if="course.instructors?.length" class="row q-col-gutter-md">
                  <div
                    v-for="i in course.instructors"
                    :key="i.id"
                    class="col-12 col-sm-6"
                  >
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
                  Valoraciones disponibles en una siguiente iteracion.
                </q-banner>
              </q-tab-panel>
            </q-tab-panels>
          </div>

          <div class="col-12 col-md-4">
            <CourseSidebar
              :level="course.level"
              :duration-text="course.durationText"
              :students-count="course.studentsCount"
              :average-rating="course.averageRating"
            />
          </div>
        </div>
      </template>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import CourseHero from '../components/CourseHero.vue';
import CourseSidebar from '../components/CourseSidebar.vue';
import CourseBreadcrumb from '../components/CourseBreadcrumb.vue';
import { getCourseBySlug, getCourseInstructors } from '../api/course';
import type { CourseDetail } from '../types/course';

const route = useRoute();
const tab = ref('descripcion');
const loading = ref(true);
const notFound = ref(false);
const errorMessage = ref('');
const course = ref<CourseDetail | null>(null);

function setOgMeta(name: string, content: string) {
  const selector = `meta[property="${name}"]`;
  let meta = document.querySelector(selector) as HTMLMetaElement | null;
  if (!meta) {
    meta = document.createElement('meta');
    meta.setAttribute('property', name);
    document.head.appendChild(meta);
  }
  meta.setAttribute('content', content);
}

function updateSeo(c: CourseDetail) {
  document.title = `${c.title} | CourselyLabs`;
  setOgMeta('og:title', c.title);
  setOgMeta('og:description', c.shortDescription || c.description || 'Curso en CourselyLabs');
  setOgMeta('og:image', c.thumbnailUrl || '');
}

async function fetchCourse() {
  loading.value = true;
  notFound.value = false;
  errorMessage.value = '';

  try {
    const slug = String(route.params.slug || '');
    const data = await getCourseBySlug(slug);

    if (!data || !data.id) {
      notFound.value = true;
      course.value = null;
      return;
    }

    const instructors = await getCourseInstructors(data.id).catch(() => []);
    course.value = { ...data, instructors };
    updateSeo(course.value);
  } catch (error: unknown) {
    const maybeStatus = (error as { response?: { status?: number } })?.response?.status;
    if (maybeStatus === 404) {
      notFound.value = true;
    } else {
      errorMessage.value = 'No se pudo cargar el curso. Intenta nuevamente.';
    }
  } finally {
    loading.value = false;
  }
}

watch(() => route.params.slug, fetchCourse);
onMounted(fetchCourse);
</script>

<style scoped>
.content-wrap {
  max-width: 1200px;
  margin: 0 auto;
}
</style>