<template>
  <q-page class="q-pa-lg">
    <h1 class="text-h5 q-mb-lg">Cursos pendientes de revisión</h1>

    <div v-if="loading">
      <q-skeleton v-for="i in 3" :key="i" type="rect" height="100px" class="q-mb-md" />
    </div>

    <div v-else-if="courses.length === 0" class="text-center q-pa-xl">
      <q-icon name="check_circle" size="64px" color="positive" />
      <div class="text-h6 text-grey-6 q-mt-md">No hay cursos pendientes</div>
      <p class="text-body2 text-grey-5">Todos los cursos han sido revisados</p>
    </div>

    <div v-else>
      <q-card v-for="course in courses" :key="course.id" flat bordered class="q-mb-md">
        <q-card-section class="row items-start q-gutter-md">
          <q-img
            v-if="course.thumbnailUrl"
            :src="course.thumbnailUrl"
            :ratio="16/9"
            style="width: 180px; border-radius: 8px"
          />
          <div v-else class="thumbnail-placeholder" style="width: 180px; border-radius: 8px" />

          <div class="col">
            <div class="text-h6">{{ course.title }}</div>
            <div class="text-caption text-grey-7 q-mb-xs">/cursos/{{ course.slug }}</div>
            <div class="text-body2 q-mb-sm">{{ course.shortDescription || 'Sin descripción corta' }}</div>
            <div class="row q-gutter-xs">
              <q-chip size="sm" dense>{{ course.level || 'Sin nivel' }}</q-chip>
              <q-chip size="sm" dense :color="course.isFree ? 'positive' : 'grey'" text-color="white">
                {{ course.isFree ? 'Gratis' : `${course.price} EUR` }}
              </q-chip>
            </div>
          </div>
        </q-card-section>

        <q-separator />

        <q-card-actions>
          <q-btn flat color="primary" icon="visibility" label="Vista previa" no-caps @click="previewCourse(course.slug)" />
          <q-space />
          <q-btn flat color="positive" icon="check" label="Aprobar y publicar" no-caps @click="handleApprove(course)" />
          <q-btn flat color="negative" icon="close" label="Rechazar" no-caps @click="openRejectDialog(course)" />
        </q-card-actions>
      </q-card>
    </div>

    <!-- Reject dialog -->
    <q-dialog v-model="rejectDialog" persistent>
      <q-card style="min-width: 450px">
        <q-card-section>
          <div class="text-h6">Rechazar curso</div>
        </q-card-section>
        <q-card-section>
          <p class="text-body2">Curso: <strong>{{ rejectCourseRef?.title }}</strong></p>
          <q-input
            v-model="rejectReason"
            label="Motivo del rechazo"
            outlined
            type="textarea"
            rows="3"
            :rules="[v => !!v || 'El motivo es obligatorio']"
          />
          <p class="text-caption text-grey-6 q-mt-xs">El instructor verá este motivo y podrá editar y reenviar el curso.</p>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn color="negative" label="Rechazar" :disable="!rejectReason" :loading="rejecting" @click="handleReject" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { getPendingCourses, approveCourse, rejectCourse as rejectCourseApi } from '../../api/admin'

const $q = useQuasar()
const loading = ref(true)
const courses = ref<any[]>([])

const rejectDialog = ref(false)
const rejectCourseRef = ref<any>(null)
const rejectReason = ref('')
const rejecting = ref(false)

function previewCourse(slug: string) {
  window.open(`/cursos/${slug}`, '_blank')
}

async function handleApprove(course: any) {
  try {
    await approveCourse(course.id)
    courses.value = courses.value.filter(c => c.id !== course.id)
    window.dispatchEvent(new CustomEvent('admin:refresh-pending'))
    $q.notify({ type: 'positive', message: `«${course.title}» aprobado y publicado`, position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al aprobar el curso', position: 'bottom-right' })
  }
}

function openRejectDialog(course: any) {
  rejectCourseRef.value = course
  rejectReason.value = ''
  rejectDialog.value = true
}

async function handleReject() {
  if (!rejectCourseRef.value || !rejectReason.value) return
  rejecting.value = true
  try {
    await rejectCourseApi(rejectCourseRef.value.id, rejectReason.value)
    courses.value = courses.value.filter(c => c.id !== rejectCourseRef.value.id)
    rejectDialog.value = false
    window.dispatchEvent(new CustomEvent('admin:refresh-pending'))
    $q.notify({ type: 'info', message: `«${rejectCourseRef.value.title}» rechazado`, position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al rechazar el curso', position: 'bottom-right' })
  } finally {
    rejecting.value = false
  }
}

onMounted(async () => {
  try {
    courses.value = await getPendingCourses()
  } catch {
    $q.notify({ type: 'negative', message: 'Error al cargar cursos pendientes', position: 'bottom-right' })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.thumbnail-placeholder {
  height: 0;
  padding-bottom: 56.25%;
  background: linear-gradient(135deg, var(--q-primary) 0%, var(--q-accent) 100%);
}
</style>
