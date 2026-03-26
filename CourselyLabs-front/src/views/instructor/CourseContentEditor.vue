<template>
  <q-page class="q-pa-lg">
    <div style="max-width: 900px; margin: 0 auto">
      <!-- Header -->
      <div class="row items-center justify-between q-mb-lg">
        <div>
          <q-btn flat dense icon="arrow_back" :to="`/instructor/cursos/${courseId}/editar`" class="q-mr-sm" />
          <span class="text-h5">Contenido del curso</span>
        </div>
        <q-btn outline color="primary" icon="visibility" label="Vista previa" no-caps @click="handlePreview" />
      </div>

      <!-- Loading -->
      <div v-if="loading">
        <q-skeleton v-for="i in 3" :key="i" type="rect" height="60px" class="q-mb-sm" />
      </div>

      <!-- Sections -->
      <div v-else>
        <div v-for="(section, sIdx) in sections" :key="section.id" class="q-mb-md">
          <q-card flat bordered>
            <q-card-section class="row items-center q-py-sm bg-grey-2">
              <q-icon name="drag_indicator" class="cursor-move text-grey-5 q-mr-sm" />

              <!-- Section title edit -->
              <q-input
                v-if="section._editing"
                v-model="section.title"
                dense
                outlined
                class="col"
                bg-color="white"
                @keyup.enter="saveSection(section)"
                @blur="saveSection(section)"
              >
                <template #after>
                  <q-btn flat dense icon="check" color="positive" @click="saveSection(section)" />
                </template>
              </q-input>
              <span v-else class="text-subtitle2 text-weight-medium col cursor-pointer" @click="section._editing = true">
                {{ section.title }}
              </span>

              <q-btn flat dense round icon="delete" color="negative" @click="confirmDeleteSection(section)" />
            </q-card-section>

            <!-- Lessons -->
            <q-list separator>
              <q-item v-for="(lesson, lIdx) in section.lessons" :key="lesson.id" class="q-pl-xl">
                <q-item-section avatar>
                  <q-icon :name="lessonTypeIcon(lesson.type)" size="20px" color="grey-7" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>{{ lesson.title }}</q-item-label>
                  <q-item-label caption>
                    {{ lessonTypeLabel(lesson.type) }}
                    <span v-if="lesson.isFree" class="text-positive"> — Gratis</span>
                  </q-item-label>
                </q-item-section>
                <q-item-section side>
                  <div class="row q-gutter-xs">
                    <q-btn flat dense round icon="edit" size="sm" @click="openLessonDialog(section.id, lesson)" />
                    <q-btn flat dense round icon="delete" size="sm" color="negative" @click="confirmDeleteLesson(section, lesson)" />
                  </div>
                </q-item-section>
              </q-item>
            </q-list>

            <q-card-section class="q-py-sm">
              <q-btn flat dense no-caps icon="add" label="Añadir leccion" color="primary" @click="openLessonDialog(section.id)" />
            </q-card-section>
          </q-card>
        </div>

        <!-- Add section -->
        <q-card flat bordered class="q-pa-sm">
          <div class="row items-center q-gutter-sm">
            <q-input
              v-model="newSectionTitle"
              dense
              outlined
              placeholder="Nombre de la nueva seccion"
              class="col"
              @keyup.enter="addSection"
            />
            <q-btn color="primary" icon="add" label="Añadir seccion" no-caps unelevated :disable="!newSectionTitle" @click="addSection" />
          </div>
        </q-card>
      </div>
    </div>

    <!-- Lesson dialog -->
    <q-dialog v-model="lessonDialog" persistent>
      <q-card style="min-width: 600px; max-width: 90vw">
        <q-card-section>
          <div class="text-h6">{{ editingLesson ? 'Editar leccion' : 'Nueva leccion' }}</div>
        </q-card-section>

        <q-card-section class="q-gutter-md">
          <q-input v-model="lessonForm.title" label="Titulo" outlined :rules="[v => !!v || 'Obligatorio']" />

          <q-select
            v-model="lessonForm.type"
            :options="lessonTypeOptions"
            label="Tipo de contenido"
            outlined
            emit-value
            map-options
          />

          <q-input v-model="lessonForm.description" label="Descripcion (opcional)" outlined type="textarea" rows="2" />

          <!-- Content by type -->
          <q-input
            v-if="lessonForm.type === 'video'"
            v-model="lessonForm.contentUrl"
            label="URL del video"
            outlined
            hint="URL directa al archivo de video (mp4, m3u8)"
          />

          <RichTextEditor
            v-if="lessonForm.type === 'text'"
            v-model="lessonForm.contentText"
            placeholder="Escribe el contenido de la leccion..."
          />

          <q-input
            v-if="lessonForm.type === 'pdf'"
            v-model="lessonForm.contentUrl"
            label="URL del PDF"
            outlined
          />

          <div class="row q-gutter-md items-center">
            <q-input v-model.number="lessonForm.duration" label="Duracion (segundos)" outlined type="number" min="0" style="width: 200px" />
            <q-toggle v-model="lessonForm.isFree" label="Leccion gratuita (preview)" />
          </div>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn
            color="primary"
            :label="editingLesson ? 'Guardar' : 'Crear'"
            :loading="savingLesson"
            :disable="!lessonForm.title"
            @click="saveLesson"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Delete confirmation -->
    <q-dialog v-model="deleteConfirmDialog">
      <q-card style="min-width: 350px">
        <q-card-section>
          <div class="text-h6">{{ deleteConfirmTitle }}</div>
        </q-card-section>
        <q-card-section>{{ deleteConfirmMessage }}</q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn flat color="negative" label="Eliminar" :loading="deleteLoading" @click="executeDelete" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useQuasar } from 'quasar'
import { getCourseSections } from '../../api/lesson'
import { getCourseForEdit } from '../../api/instructor'
import { createSection, updateSection, deleteSection } from '../../api/sectionEditor'
import { createLesson, updateLesson, deleteLesson } from '../../api/lessonEditor'
import RichTextEditor from '../../components/RichTextEditor.vue'

const route = useRoute()
const $q = useQuasar()

const courseId = computed(() => route.params.id as string)
const loading = ref(true)
const sections = ref<any[]>([])
const courseSlug = ref('')

// New section
const newSectionTitle = ref('')

// Lesson dialog
const lessonDialog = ref(false)
const editingLesson = ref<any>(null)
const currentSectionId = ref('')
const savingLesson = ref(false)

const lessonForm = ref({
  title: '',
  type: 'text',
  description: '',
  contentText: '',
  contentUrl: '',
  duration: 0,
  isFree: false,
})

const lessonTypeOptions = [
  { label: 'Texto', value: 'text' },
  { label: 'Video', value: 'video' },
  { label: 'PDF', value: 'pdf' },
]

// Delete confirmation
const deleteConfirmDialog = ref(false)
const deleteConfirmTitle = ref('')
const deleteConfirmMessage = ref('')
const deleteLoading = ref(false)
let deleteFn: (() => Promise<void>) | null = null

function lessonTypeIcon(type: string) {
  switch (type) {
    case 'video': return 'play_circle'
    case 'pdf': return 'picture_as_pdf'
    default: return 'article'
  }
}

function lessonTypeLabel(type: string) {
  switch (type) {
    case 'video': return 'Video'
    case 'pdf': return 'PDF'
    default: return 'Texto'
  }
}

function handlePreview() {
  window.open(`/cursos/${courseSlug.value}`, '_blank')
}

async function addSection() {
  if (!newSectionTitle.value) return
  try {
    const created = await createSection(courseId.value, {
      title: newSectionTitle.value,
      position: sections.value.length,
    })
    created.lessons = []
    created._editing = false
    sections.value.push(created)
    newSectionTitle.value = ''
  } catch {
    $q.notify({ type: 'negative', message: 'Error al crear la seccion', position: 'bottom-right' })
  }
}

async function saveSection(section: any) {
  section._editing = false
  try {
    await updateSection(section.id, { title: section.title, position: section.position })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al actualizar la seccion', position: 'bottom-right' })
  }
}

function confirmDeleteSection(section: any) {
  deleteConfirmTitle.value = 'Eliminar seccion'
  deleteConfirmMessage.value = `¿Eliminar la seccion "${section.title}"? Se eliminaran todas sus lecciones.`
  deleteFn = async () => {
    await deleteSection(section.id)
    sections.value = sections.value.filter(s => s.id !== section.id)
  }
  deleteConfirmDialog.value = true
}

function confirmDeleteLesson(section: any, lesson: any) {
  deleteConfirmTitle.value = 'Eliminar leccion'
  deleteConfirmMessage.value = `¿Eliminar la leccion "${lesson.title}"?`
  deleteFn = async () => {
    await deleteLesson(lesson.id)
    section.lessons = section.lessons.filter((l: any) => l.id !== lesson.id)
  }
  deleteConfirmDialog.value = true
}

async function executeDelete() {
  if (!deleteFn) return
  deleteLoading.value = true
  try {
    await deleteFn()
    deleteConfirmDialog.value = false
    $q.notify({ type: 'positive', message: 'Eliminado', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al eliminar', position: 'bottom-right' })
  } finally {
    deleteLoading.value = false
  }
}

function openLessonDialog(sectionId: string, lesson?: any) {
  currentSectionId.value = sectionId
  editingLesson.value = lesson || null
  lessonForm.value = {
    title: lesson?.title || '',
    type: lesson?.type || 'text',
    description: lesson?.description || '',
    contentText: lesson?.contentText || '',
    contentUrl: lesson?.contentUrl || '',
    duration: lesson?.duration || 0,
    isFree: lesson?.isFree || false,
  }
  lessonDialog.value = true
}

async function saveLesson() {
  savingLesson.value = true
  try {
    const payload = {
      title: lessonForm.value.title,
      type: lessonForm.value.type,
      description: lessonForm.value.description || undefined,
      contentText: lessonForm.value.type === 'text' ? lessonForm.value.contentText : undefined,
      contentUrl: ['video', 'pdf'].includes(lessonForm.value.type) ? lessonForm.value.contentUrl : undefined,
      duration: lessonForm.value.duration || undefined,
      isFree: lessonForm.value.isFree,
    }

    if (editingLesson.value) {
      const updated = await updateLesson(editingLesson.value.id, payload)
      const section = sections.value.find(s => s.id === currentSectionId.value)
      if (section) {
        const idx = section.lessons.findIndex((l: any) => l.id === editingLesson.value.id)
        if (idx >= 0) section.lessons[idx] = updated
      }
    } else {
      const section = sections.value.find(s => s.id === currentSectionId.value)
      const created = await createLesson(currentSectionId.value, {
        ...payload,
        position: section?.lessons.length || 0,
      })
      section?.lessons.push(created)
    }
    lessonDialog.value = false
    $q.notify({ type: 'positive', message: editingLesson.value ? 'Leccion actualizada' : 'Leccion creada', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al guardar la leccion', position: 'bottom-right' })
  } finally {
    savingLesson.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const course = await getCourseForEdit(courseId.value)
    courseSlug.value = course.slug
    const raw = await getCourseSections(courseId.value)
    sections.value = raw.map((s: any) => ({ ...s, _editing: false }))
  } catch {
    $q.notify({ type: 'negative', message: 'Error al cargar el contenido', position: 'bottom-right' })
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
