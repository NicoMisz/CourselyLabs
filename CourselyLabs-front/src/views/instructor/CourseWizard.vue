<template>
  <q-page class="q-pa-lg">
    <div style="max-width: 800px; margin: 0 auto">
      <h1 class="text-h5 q-mb-lg">{{ isEditing ? 'Editar curso' : 'Crear nuevo curso' }}</h1>

      <q-stepper v-model="step" ref="stepperRef" animated vertical flat bordered>
        <!-- Step 1: Info basica -->
        <q-step :name="1" title="Informacion basica" icon="info" :done="step > 1">
          <div class="q-gutter-md">
            <q-input
              v-model="form.title"
              label="Titulo del curso"
              outlined
              :rules="[v => !!v || 'El titulo es obligatorio']"
              @update:model-value="generateSlug"
            />
            <q-input
              v-model="form.slug"
              label="Slug (URL)"
              outlined
              hint="Se genera automaticamente desde el titulo"
              :rules="[v => !!v || 'El slug es obligatorio']"
            />
            <q-select
              v-model="form.categoryId"
              :options="categoryOptions"
              label="Categoria"
              outlined
              emit-value
              map-options
            />
            <q-select
              v-model="form.level"
              :options="levelOptions"
              label="Nivel"
              outlined
              emit-value
              map-options
            />
            <q-input
              v-model="form.shortDescription"
              label="Descripcion corta"
              outlined
              type="textarea"
              rows="2"
              maxlength="500"
              counter
            />
          </div>
          <q-stepper-navigation>
            <q-btn color="primary" label="Siguiente" @click="goStep(2)" :disable="!form.title || !form.slug" />
          </q-stepper-navigation>
        </q-step>

        <!-- Step 2: Detalles -->
        <q-step :name="2" title="Detalles" icon="description" :done="step > 2">
          <div class="q-gutter-md">
            <div class="text-subtitle2 q-mb-xs">Descripcion completa</div>
            <RichTextEditor v-model="form.description" placeholder="Describe tu curso en detalle..." />

            <div>
              <div class="text-caption text-grey-7 q-mb-xs">Thumbnail del curso</div>
              <div v-if="form.thumbnailUrl" class="q-mb-sm">
                <img :src="form.thumbnailUrl" style="max-width: 200px; border-radius: 8px" />
              </div>
              <FileUploader
                v-if="isEditing"
                ref="thumbnailUploaderRef"
                accept="image/jpeg,image/png,image/webp,image/gif"
                :max-size-mb="5"
                label="Subir imagen"
                hint="JPG, PNG, WebP, GIF hasta 5 MB"
                icon="image"
                @upload="handleThumbnailUpload"
              />
              <q-input
                v-else
                v-model="form.thumbnailUrl"
                label="URL del thumbnail"
                outlined
                hint="Guarda el curso primero para subir imagenes directamente"
              />
            </div>

            <div class="row q-gutter-md items-center">
              <q-toggle v-model="form.isFree" label="Curso gratuito" />
              <q-input
                v-if="!form.isFree"
                v-model.number="form.price"
                label="Precio (EUR)"
                outlined
                type="number"
                min="0"
                step="0.01"
                style="width: 160px"
              />
            </div>
          </div>
          <q-stepper-navigation>
            <q-btn color="primary" label="Siguiente" @click="goStep(3)" :disable="!form.description" />
            <q-btn flat label="Atras" @click="goStep(1)" class="q-ml-sm" />
          </q-stepper-navigation>
        </q-step>

        <!-- Step 3: Contenido -->
        <q-step :name="3" title="Contenido" icon="list" :done="step > 3">
          <div v-if="isEditing" class="q-pa-md text-center">
            <p class="text-body2 text-grey-7">Gestiona las secciones y lecciones desde el editor de contenido.</p>
            <q-btn
              outline
              color="primary"
              label="Ir al editor de contenido"
              icon="edit"
              :to="`/instructor/cursos/${courseId}/contenido`"
              no-caps
            />
          </div>
          <div v-else class="q-pa-md text-center">
            <q-icon name="info" size="32px" color="grey-5" class="q-mb-sm" />
            <p class="text-body2 text-grey-7">
              Primero guarda el curso como borrador. Despues podras añadir secciones y lecciones desde el editor de contenido.
            </p>
          </div>
          <q-stepper-navigation>
            <q-btn color="primary" label="Siguiente" @click="goStep(4)" />
            <q-btn flat label="Atras" @click="goStep(2)" class="q-ml-sm" />
          </q-stepper-navigation>
        </q-step>

        <!-- Step 4: Revision -->
        <q-step :name="4" title="Revision" icon="check_circle">
          <div class="q-gutter-sm">
            <div class="text-subtitle2">Resumen del curso</div>
            <q-markup-table flat bordered>
              <tbody>
                <tr><td class="text-weight-medium">Titulo</td><td>{{ form.title }}</td></tr>
                <tr><td class="text-weight-medium">Slug</td><td>/cursos/{{ form.slug }}</td></tr>
                <tr><td class="text-weight-medium">Nivel</td><td>{{ form.level || 'Sin especificar' }}</td></tr>
                <tr><td class="text-weight-medium">Precio</td><td>{{ form.isFree ? 'Gratis' : `${form.price || 0} EUR` }}</td></tr>
              </tbody>
            </q-markup-table>
          </div>
          <q-stepper-navigation>
            <q-btn
              color="primary"
              :label="isEditing ? 'Guardar cambios' : 'Guardar borrador'"
              icon="save"
              :loading="saving"
              @click="handleSave"
              no-caps
            />
            <q-btn flat label="Atras" @click="goStep(3)" class="q-ml-sm" />
          </q-stepper-navigation>
        </q-step>
      </q-stepper>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useQuasar } from 'quasar'
import { createCourse, updateCourse, getCourseForEdit } from '../../api/instructor'
import { getCategories } from '../../api/course'
import RichTextEditor from '../../components/RichTextEditor.vue'
import FileUploader from '../../components/FileUploader.vue'
import { uploadCourseThumbnail } from '../../api/resources'

const route = useRoute()
const router = useRouter()
const $q = useQuasar()

const courseId = computed(() => route.params.id as string | undefined)
const isEditing = computed(() => !!courseId.value)

const step = ref(1)
const saving = ref(false)

const form = ref({
  title: '',
  slug: '',
  description: '',
  shortDescription: '',
  thumbnailUrl: '',
  categoryId: null as number | null,
  level: 'beginner',
  isFree: true,
  price: 0,
})

const categoryOptions = ref<{ label: string; value: number }[]>([])

const levelOptions = [
  { label: 'Principiante', value: 'beginner' },
  { label: 'Intermedio', value: 'intermediate' },
  { label: 'Avanzado', value: 'advanced' },
]

function generateSlug() {
  if (!isEditing.value) {
    form.value.slug = form.value.title
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9\s-]/g, '')
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '')
  }
}

function goStep(s: number) {
  step.value = s
}

const thumbnailUploaderRef = ref<InstanceType<typeof FileUploader> | null>(null)

async function handleThumbnailUpload(file: File, onProgress: (pct: number) => void) {
  if (!courseId.value) return
  try {
    const url = await uploadCourseThumbnail(courseId.value, file, onProgress)
    form.value.thumbnailUrl = url
    thumbnailUploaderRef.value?.finish()
    $q.notify({ type: 'positive', message: 'Thumbnail subido', position: 'bottom-right' })
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Error al subir thumbnail'
    thumbnailUploaderRef.value?.finish(msg)
    $q.notify({ type: 'negative', message: msg, position: 'bottom-right' })
  }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = {
      title: form.value.title,
      slug: form.value.slug,
      description: form.value.description,
      shortDescription: form.value.shortDescription || undefined,
      thumbnailUrl: form.value.thumbnailUrl || undefined,
      categoryId: form.value.categoryId || undefined,
      level: form.value.level,
      isFree: form.value.isFree,
      price: form.value.isFree ? undefined : form.value.price,
    }

    if (isEditing.value) {
      await updateCourse(courseId.value!, payload)
      $q.notify({ type: 'positive', message: 'Curso actualizado', position: 'bottom-right' })
    } else {
      const created = await createCourse(payload)
      $q.notify({ type: 'positive', message: 'Curso creado como borrador', position: 'bottom-right' })
      router.push(`/instructor/cursos/${created.id}/contenido`)
    }
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Error al guardar el curso'
    $q.notify({ type: 'negative', message: msg, position: 'bottom-right' })
  } finally {
    saving.value = false
  }
}

async function loadCategories() {
  try {
    const cats = await getCategories()
    categoryOptions.value = cats.map((c: any) => ({ label: c.name, value: c.id }))
  } catch { /* ignore */ }
}

async function loadCourse() {
  if (!courseId.value) return
  try {
    const course = await getCourseForEdit(courseId.value)
    form.value = {
      title: course.title,
      slug: course.slug,
      description: course.description || '',
      shortDescription: course.shortDescription || '',
      thumbnailUrl: course.thumbnailUrl || '',
      categoryId: course.categoryId || null,
      level: course.level || 'beginner',
      isFree: course.isFree ?? true,
      price: course.price || 0,
    }
  } catch {
    $q.notify({ type: 'negative', message: 'Error al cargar el curso', position: 'bottom-right' })
    router.push('/instructor/cursos')
  }
}

onMounted(async () => {
  await loadCategories()
  await loadCourse()
})
</script>
