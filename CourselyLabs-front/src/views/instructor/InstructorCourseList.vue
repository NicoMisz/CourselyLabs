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
        :disable="!canCreate || (limits !== null && limits.currentCourses >= limits.maxCourses && limits.maxCourses !== 2147483647)"
        @click="openCreateDialog"
      />
    </div>

    <!-- Banner: email no verificado -->
    <q-banner
      v-if="!canCreate"
      rounded
      class="bg-amber-1 text-amber-9 q-mb-md"
      inline-actions
    >
      <template #avatar>
        <q-icon name="mail_outline" color="amber-9" />
      </template>
      <span class="text-body2">
        Para crear cursos, primero verifica tu correo electrónico. Revisa tu bandeja de entrada o reenvía el correo desde tu perfil.
      </span>
      <template #action>
        <q-btn flat dense color="amber-9" label="Ir a mi perfil" no-caps to="/profile" />
      </template>
    </q-banner>

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
      <q-btn color="primary" label="Nuevo curso" icon="add" no-caps unelevated class="q-mt-sm" :disable="!canCreate" @click="openCreateDialog" />
    </div>

    <!-- Course list -->
    <div v-else class="row q-gutter-md">
      <div v-for="course in courses" :key="course.id" class="col-12 col-sm-6 col-md-4">
        <q-card class="course-card" flat bordered>
          <CourseThumbnail
            :thumbnail-url="course.thumbnailUrl"
            :title="course.title"
            :rounded="false"
          />

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

            <!-- Rejection banner -->
            <q-banner v-if="course.status === 'rejected' && course.rejectionReason" rounded class="bg-red-1 text-negative q-mt-sm" dense>
              <template #avatar><q-icon name="warning" color="negative" /></template>
              {{ course.rejectionReason }}
            </q-banner>
          </q-card-section>

          <q-separator />

          <q-card-actions>
            <q-btn flat dense no-caps icon="edit" label="Editar" :to="`/instructor/cursos/${course.id}/editar`" />
            <q-space />
            <q-btn flat dense round icon="more_vert">
              <q-menu>
                <q-list dense>
                  <q-item
                    v-if="course.status === 'draft'"
                    clickable
                    v-close-popup
                    @click="openSubmitDialog(course)"
                  >
                    <q-item-section avatar><q-icon name="send" size="20px" /></q-item-section>
                    <q-item-section>Enviar a revisión</q-item-section>
                  </q-item>
                  <q-item
                    v-if="course.status === 'rejected'"
                    clickable
                    v-close-popup
                    @click="openSubmitDialog(course)"
                  >
                    <q-item-section avatar><q-icon name="replay" size="20px" color="warning" /></q-item-section>
                    <q-item-section>Editar y reenviar</q-item-section>
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
          ¿Estás seguro de que quieres eliminar «{{ courseToDelete?.title }}»? Esta acción no se puede deshacer.
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn flat color="negative" label="Eliminar" :loading="deleting" @click="handleDelete" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Quick create dialog -->
    <q-dialog v-model="createDialog" persistent>
      <q-card style="min-width: 400px">
        <q-card-section>
          <div class="text-h6">Nuevo curso</div>
          <p class="text-body2 text-grey-7 q-mt-xs q-mb-none">
            Empieza con un título. Podras completar el resto desde el editor.
          </p>
        </q-card-section>
        <q-card-section>
          <q-input
            v-model="newCourseTitle"
            label="Título del curso"
            outlined
            autofocus
            :rules="[v => v.length >= 3 || 'Mínimo 3 caracteres']"
            @keyup.enter="handleCreate"
          />
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup :disable="creating" />
          <q-btn
            color="primary"
            label="Crear borrador"
            unelevated
            :loading="creating"
            :disable="newCourseTitle.length < 3"
            @click="handleCreate"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Submit review dialog -->
    <q-dialog v-model="submitDialog">
      <q-card style="min-width: 400px">
        <q-card-section>
          <div class="text-h6">Enviar a revisión</div>
        </q-card-section>
        <q-card-section>
          <div class="text-body2 q-mb-md">Tu curso será revisado por un administrador. Si es aprobado, se publicara automáticamente. Verifica que cumple los requisitos:</div>
          <q-list dense>
            <q-item v-for="check in submitChecks" :key="check.label">
              <q-item-section avatar>
                <q-icon :name="check.ok ? 'check_circle' : 'cancel'" :color="check.ok ? 'positive' : 'negative'" />
              </q-item-section>
              <q-item-section>{{ check.label }}</q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn
            color="primary"
            label="Enviar a revisión"
            :disable="!allChecksPass"
            :loading="submitting"
            @click="handleSubmitReview"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { useRouter } from 'vue-router'
import { getMyCreatedCourses, getMyCourseLimits, deleteCourse, submitForReview, createCourse } from '../../api/instructor'
import { getCourseSections } from '../../api/lesson'
import type { CourseLimits } from '../../api/instructor'
import { useAuthStore } from '@/stores/auth'
import CourseThumbnail from '@/components/CourseThumbnail.vue'

const $q = useQuasar()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(true)
const courses = ref<any[]>([])
const limits = ref<CourseLimits | null>(null)

const canCreate = computed(() => {
  const u = authStore.user
  if (!u) return false
  return u.role === 'admin' || u.isVerified === true
})
const deleteDialog = ref(false)
const courseToDelete = ref<any>(null)

// Quick create
const createDialog = ref(false)
const newCourseTitle = ref('')
const creating = ref(false)

function openCreateDialog() {
  newCourseTitle.value = ''
  createDialog.value = true
}

function slugify(text: string): string {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
    .slice(0, 80) || 'curso-' + Date.now()
}

async function handleCreate() {
  if (newCourseTitle.value.length < 3) return
  creating.value = true
  try {
    const payload = {
      title: newCourseTitle.value,
      slug: slugify(newCourseTitle.value) + '-' + Math.random().toString(36).slice(2, 6),
      description: '',
      isFree: true,
    }
    const created = await createCourse(payload)
    createDialog.value = false
    $q.notify({ type: 'positive', message: 'Borrador creado', position: 'bottom-right' })
    router.push(`/instructor/cursos/${created.id}/editar`)
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Error al crear el curso'
    $q.notify({ type: 'negative', message: msg, position: 'bottom-right' })
  } finally {
    creating.value = false
  }
}

// Submit review
const submitDialog = ref(false)
const submitCourseId = ref('')
const submitting = ref(false)
const submitChecks = ref<{ label: string; ok: boolean }[]>([])
const allChecksPass = computed(() => submitChecks.value.every(c => c.ok))
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
    case 'pending_review': return 'En revisión'
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

async function openSubmitDialog(course: any) {
  submitCourseId.value = course.id
  submitChecks.value = [
    { label: 'El curso tiene título', ok: !!course.title },
    { label: 'Cargando contenido...', ok: false },
  ]
  submitDialog.value = true

  try {
    const sections = await getCourseSections(course.id)
    const totalLessons = sections.reduce((sum: number, s: any) => sum + (s.lessons?.length || 0), 0)
    submitChecks.value = [
      { label: 'El curso tiene título', ok: !!course.title },
      { label: `Al menos 1 sección (${sections.length} encontradas)`, ok: sections.length > 0 },
      { label: `Al menos 1 lección (${totalLessons} encontradas)`, ok: totalLessons > 0 },
    ]
  } catch {
    submitChecks.value = [
      { label: 'El curso tiene título', ok: !!course.title },
      { label: 'Error al verificar contenido', ok: false },
    ]
  }
}

async function handleSubmitReview() {
  submitting.value = true
  try {
    await submitForReview(submitCourseId.value)
    const course = courses.value.find(c => c.id === submitCourseId.value)
    if (course) course.status = 'pending_review'
    submitDialog.value = false
    $q.notify({ type: 'positive', message: 'Curso enviado a revisión', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al enviar a revisión', position: 'bottom-right' })
  } finally {
    submitting.value = false
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
</style>
