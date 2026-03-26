<template>
  <q-page class="q-pa-lg">
    <div class="row items-center justify-between q-mb-lg">
      <div>
        <h1 class="text-h5 q-my-none">Mis cursos</h1>
        <p v-if="limits" class="text-caption text-grey-7 q-mt-xs q-mb-none">
          {{ limits.currentCourses }} de {{ limits.maxCourses === 2147483647 ? '∞' : limits.maxCourses }} cursos creados
        </p>
      </div>
      <q-btn
        color="primary"
        icon="add"
        label="Nuevo curso"
        no-caps
        unelevated
        to="/instructor/cursos/nuevo"
        :disable="limits !== null && limits.currentCourses >= limits.maxCourses && limits.maxCourses !== 2147483647"
      />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="row q-gutter-md">
      <div v-for="i in 3" :key="i" class="col-12 col-sm-6 col-md-4">
        <q-skeleton type="rect" height="200px" />
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="courses.length === 0" class="text-center q-pa-xl">
      <q-icon name="add_circle" size="64px" color="grey-4" />
      <div class="text-h6 text-grey-6 q-mt-md">Crea tu primer curso</div>
      <p class="text-body2 text-grey-5">Comparte tus conocimientos con el mundo</p>
      <q-btn color="primary" label="Nuevo curso" icon="add" to="/instructor/cursos/nuevo" no-caps unelevated class="q-mt-sm" />
    </div>

    <!-- Course list -->
    <div v-else class="row q-gutter-md">
      <div v-for="course in courses" :key="course.id" class="col-12 col-sm-6 col-md-4">
        <q-card class="course-card" flat bordered>
          <q-img
            v-if="course.thumbnailUrl"
            :src="course.thumbnailUrl"
            :ratio="16/9"
          />
          <div v-else class="thumbnail-placeholder" />

          <q-card-section>
            <div class="row items-center q-gutter-xs q-mb-sm">
              <q-chip
                :color="statusColor(course.status)"
                text-color="white"
                size="sm"
                dense
              >
                {{ statusLabel(course.status) }}
              </q-chip>
              <q-chip v-if="course.isFree" color="positive" text-color="white" size="sm" dense>
                Gratis
              </q-chip>
            </div>
            <div class="text-subtitle1 text-weight-medium ellipsis-2-lines">{{ course.title }}</div>
            <div class="text-caption text-grey-7 q-mt-xs">
              {{ course.totalStudents || 0 }} estudiantes
            </div>
          </q-card-section>

          <q-separator />

          <q-card-actions>
            <q-btn flat dense no-caps icon="edit" label="Editar" :to="`/instructor/cursos/${course.id}/editar`" />
            <q-btn flat dense no-caps icon="list" label="Contenido" :to="`/instructor/cursos/${course.id}/contenido`" />
            <q-space />
            <q-btn flat dense round icon="more_vert">
              <q-menu>
                <q-list dense>
                  <q-item
                    v-if="course.status === 'draft'"
                    clickable
                    v-close-popup
                    @click="handleSubmitReview(course.id)"
                  >
                    <q-item-section avatar><q-icon name="send" size="20px" /></q-item-section>
                    <q-item-section>Enviar a revision</q-item-section>
                  </q-item>
                  <q-item clickable v-close-popup @click="handlePreview(course.slug)">
                    <q-item-section avatar><q-icon name="visibility" size="20px" /></q-item-section>
                    <q-item-section>Vista previa</q-item-section>
                  </q-item>
                  <q-separator />
                  <q-item clickable v-close-popup @click="confirmDelete(course)">
                    <q-item-section avatar><q-icon name="delete" size="20px" color="negative" /></q-item-section>
                    <q-item-section class="text-negative">Eliminar</q-item-section>
                  </q-item>
                </q-list>
              </q-menu>
            </q-btn>
          </q-card-actions>
        </q-card>
      </div>
    </div>

    <!-- Delete dialog -->
    <q-dialog v-model="deleteDialog">
      <q-card style="min-width: 350px">
        <q-card-section>
          <div class="text-h6">Eliminar curso</div>
        </q-card-section>
        <q-card-section>
          ¿Estas seguro de que quieres eliminar "{{ courseToDelete?.title }}"? Esta accion no se puede deshacer.
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn flat color="negative" label="Eliminar" :loading="deleting" @click="handleDelete" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { getMyCreatedCourses, getMyCourseLimits, deleteCourse, submitForReview } from '../../api/instructor'
import type { CourseLimits } from '../../api/instructor'

const $q = useQuasar()
const loading = ref(true)
const courses = ref<any[]>([])
const limits = ref<CourseLimits | null>(null)
const deleteDialog = ref(false)
const courseToDelete = ref<any>(null)
const deleting = ref(false)

function statusColor(status: string) {
  switch (status) {
    case 'published': return 'positive'
    case 'pending_review': return 'warning'
    case 'rejected': return 'negative'
    default: return 'grey'
  }
}

function statusLabel(status: string) {
  switch (status) {
    case 'published': return 'Publicado'
    case 'pending_review': return 'En revision'
    case 'rejected': return 'Rechazado'
    default: return 'Borrador'
  }
}

function handlePreview(slug: string) {
  window.open(`/cursos/${slug}`, '_blank')
}

function confirmDelete(course: any) {
  courseToDelete.value = course
  deleteDialog.value = true
}

async function handleDelete() {
  if (!courseToDelete.value) return
  deleting.value = true
  try {
    await deleteCourse(courseToDelete.value.id)
    courses.value = courses.value.filter(c => c.id !== courseToDelete.value.id)
    deleteDialog.value = false
    $q.notify({ type: 'positive', message: 'Curso eliminado', position: 'bottom-right' })
    limits.value = await getMyCourseLimits()
  } catch {
    $q.notify({ type: 'negative', message: 'Error al eliminar el curso', position: 'bottom-right' })
  } finally {
    deleting.value = false
  }
}

async function handleSubmitReview(id: string) {
  try {
    await submitForReview(id)
    const course = courses.value.find(c => c.id === id)
    if (course) course.status = 'pending_review'
    $q.notify({ type: 'positive', message: 'Curso enviado a revision', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al enviar a revision', position: 'bottom-right' })
  }
}

async function loadData() {
  loading.value = true
  try {
    [courses.value, limits.value] = await Promise.all([
      getMyCreatedCourses(),
      getMyCourseLimits(),
    ])
  } catch {
    $q.notify({ type: 'negative', message: 'Error al cargar tus cursos', position: 'bottom-right' })
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.course-card {
  transition: box-shadow 0.2s ease;
}
.course-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.thumbnail-placeholder {
  height: 0;
  padding-bottom: 56.25%;
  background: linear-gradient(135deg, #0f766e 0%, #ea580c 100%);
}
</style>
