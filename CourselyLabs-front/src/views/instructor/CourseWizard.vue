<template>
  <q-page class="edit-page">
    <div v-if="loadingCourse" class="text-center q-pa-xl">
      <q-spinner-dots color="primary" size="40px" />
    </div>

    <template v-else-if="course">
      <!-- Top bar -->
      <div class="edit-topbar">
        <div class="row items-center q-gutter-sm">
          <q-btn flat dense icon="arrow_back" to="/instructor/cursos" />
          <div>
            <div class="text-caption text-grey-6">Editando</div>
            <div class="text-subtitle1 text-weight-medium ellipsis" style="max-width: 500px">{{ course.title }}</div>
          </div>
          <q-chip
            v-if="course.status"
            :color="statusColor(course.status)"
            text-color="white"
            size="sm"
            dense
          >
            {{ statusLabel(course.status) }}
          </q-chip>
        </div>
        <div class="row q-gutter-sm items-center">
          <q-btn flat color="primary" icon="visibility" label="Vista previa" no-caps @click="openPreview" />
          <q-btn
            v-if="course.status === 'draft' || course.status === 'rejected'"
            color="accent"
            icon="send"
            label="Enviar a revision"
            unelevated
            no-caps
            @click="openSubmitDialog"
          />
        </div>
      </div>

      <div class="edit-layout">
        <!-- Sidebar -->
        <aside class="edit-sidebar">
          <q-list dense>
            <q-item
              clickable
              :active="selected?.type === 'info'"
              active-class="active-item"
              @click="selected = { type: 'info' }"
            >
              <q-item-section avatar><q-icon name="info" /></q-item-section>
              <q-item-section>Informacion basica</q-item-section>
            </q-item>
            <q-item
              clickable
              :active="selected?.type === 'details'"
              active-class="active-item"
              @click="selected = { type: 'details' }"
            >
              <q-item-section avatar><q-icon name="description" /></q-item-section>
              <q-item-section>Detalles</q-item-section>
            </q-item>
            <q-item
              clickable
              :active="selected?.type === 'prerequisites'"
              active-class="active-item"
              @click="selected = { type: 'prerequisites' }"
            >
              <q-item-section avatar><q-icon name="account_tree" /></q-item-section>
              <q-item-section>Prerequisitos</q-item-section>
            </q-item>
          </q-list>

          <q-separator class="q-my-sm" />

          <div class="q-px-md q-pb-xs text-caption text-weight-medium text-grey-7">
            CONTENIDO
          </div>

          <q-list dense>
            <template v-for="section in sections" :key="section.id">
              <q-item
                clickable
                :active="selected?.type === 'section' && selected.id === section.id"
                active-class="active-item"
                @click="selected = { type: 'section', id: section.id }"
              >
                <q-item-section avatar><q-icon name="folder" /></q-item-section>
                <q-item-section>
                  <q-item-label lines="1">{{ section.title }}</q-item-label>
                </q-item-section>
              </q-item>

              <q-item
                v-for="lesson in section.lessons"
                :key="lesson.id"
                clickable
                dense
                :active="selected?.type === 'lesson' && selected.id === lesson.id"
                active-class="active-item"
                class="lesson-item"
                @click="selected = { type: 'lesson', id: lesson.id, sectionId: section.id }"
              >
                <q-item-section avatar>
                  <q-icon :name="lessonTypeIcon(lesson.type)" size="18px" />
                </q-item-section>
                <q-item-section>
                  <q-item-label lines="1" class="text-body2">{{ lesson.title }}</q-item-label>
                </q-item-section>
              </q-item>
            </template>
          </q-list>

          <div class="q-pa-sm">
            <q-btn
              flat
              dense
              no-caps
              icon="add"
              label="Nueva seccion"
              color="primary"
              class="full-width"
              @click="showAddSection = true"
            />
          </div>

          <!-- Storage indicator -->
          <div class="storage-panel">
            <div class="row items-center justify-between q-mb-xs">
              <span class="text-caption text-weight-medium text-grey-7">
                <q-icon name="storage" size="14px" /> Almacenamiento
              </span>
              <span class="text-caption text-grey-6">{{ storageLabel }}</span>
            </div>
            <q-linear-progress
              :value="storageRatio"
              :color="storageColor"
              track-color="grey-3"
              size="6px"
              rounded
            />
            <div v-if="!isUnlimited" class="text-caption text-grey-6 q-mt-xs">
              {{ Math.round(storageRatio * 100) }}% usado
            </div>
          </div>
        </aside>

        <!-- Main panel -->
        <main class="edit-main">
          <!-- Info basica -->
          <div v-if="selected?.type === 'info'" class="panel">
            <h2 class="panel-title">Informacion basica</h2>
            <q-input v-model="info.title" label="Titulo" outlined />
            <q-input v-model="info.slug" label="Slug (URL)" outlined hint="/cursos/[slug]" />
            <q-input v-model="info.shortDescription" label="Descripcion corta" outlined type="textarea" rows="2" maxlength="500" counter />
            <q-select
              v-model="info.categoryId"
              :options="categoryOptions"
              label="Categoria"
              outlined
              emit-value map-options clearable
            />
            <q-select
              v-model="info.level"
              :options="levelOptions"
              label="Nivel"
              outlined
              emit-value map-options
            />
            <div class="panel-actions">
              <q-btn color="primary" unelevated no-caps label="Guardar cambios" :loading="saving" @click="saveInfo" />
            </div>
          </div>

          <!-- Detalles -->
          <div v-else-if="selected?.type === 'details'" class="panel">
            <h2 class="panel-title">Detalles del curso</h2>

            <div>
              <label class="panel-label">Descripcion completa</label>
              <RichTextEditor v-model="details.description" placeholder="Describe tu curso con detalle..." />
            </div>

            <div>
              <label class="panel-label">Thumbnail</label>
              <div v-if="details.thumbnailUrl" class="thumbnail-preview">
                <img :src="details.thumbnailUrl" />
                <q-btn flat dense icon="delete" color="negative" @click="details.thumbnailUrl = ''">
                  <q-tooltip>Quitar thumbnail</q-tooltip>
                </q-btn>
              </div>
              <FileUploader
                ref="thumbnailUploaderRef"
                accept="image/jpeg,image/png,image/webp,image/gif"
                :max-size-mb="5"
                label="Subir imagen"
                hint="JPG, PNG, WebP o GIF hasta 5 MB"
                icon="image"
                button-label="Elegir imagen"
                @upload="handleThumbnailUpload"
              />
            </div>

            <div class="q-mt-md">
              <q-toggle
                v-model="details.isPremium"
                label="Curso Premium"
                :disable="!canUsePremiumFeatures"
              />
              <div class="text-caption text-grey-7 q-mt-xs">
                {{ details.isPremium
                  ? 'Solo accesible con suscripcion Premium activa.'
                  : 'Accesible para cualquier usuario autenticado.' }}
              </div>
              <q-banner v-if="!canUsePremiumFeatures" rounded class="bg-amber-1 q-mt-sm" dense>
                <template #avatar>
                  <q-icon name="workspace_premium" color="amber-8" />
                </template>
                Crear cursos Premium es una funcion exclusiva para miembros Premium.
                <template #action>
                  <q-btn flat dense color="amber-8" label="Hazte Premium" no-caps to="/premium" />
                </template>
              </q-banner>
            </div>

            <div class="panel-actions">
              <q-btn color="primary" unelevated no-caps label="Guardar cambios" :loading="saving" @click="saveDetails" />
            </div>
          </div>

          <!-- Prerequisitos -->
          <div v-else-if="selected?.type === 'prerequisites'" class="panel">
            <h2 class="panel-title">Prerequisitos</h2>
            <p class="text-body2 text-grey-7">
              Los estudiantes necesitaran completar estos cursos antes de poder inscribirse en este.
              Ideal para crear rutas de aprendizaje.
            </p>

            <!-- Locked for non-premium -->
            <div v-if="!canUsePremiumFeatures" class="premium-lock">
              <q-icon name="workspace_premium" size="48px" color="amber-8" />
              <div class="text-h6 q-mt-md">Funcion exclusiva Premium</div>
              <p class="text-body2 text-grey-7 q-mb-md" style="max-width: 480px; margin-left: auto; margin-right: auto">
                Enlazar cursos con prerequisitos es una de las ventajas de la suscripcion Premium.
                Puedes seguir creando tu curso con el resto de funciones.
              </p>
              <q-btn
                color="amber-8"
                text-color="white"
                unelevated
                no-caps
                icon="workspace_premium"
                label="Hazte Premium"
                to="/premium"
              />
            </div>

            <template v-else>
              <q-select
                v-model="prerequisiteIds"
                :options="prerequisiteOptions"
                label="Cursos requeridos"
                outlined
                use-input
                input-debounce="300"
                multiple
                use-chips
                emit-value
                map-options
                @filter="filterPrerequisiteOptions"
                :loading="loadingPrereqOptions"
              >
                <template #no-option>
                  <q-item>
                    <q-item-section class="text-grey-6">Sin resultados</q-item-section>
                  </q-item>
                </template>
              </q-select>

              <q-list v-if="prerequisiteIds.length > 0" bordered separator class="q-mt-md">
                <q-item v-for="id in prerequisiteIds" :key="id">
                  <q-item-section avatar>
                    <q-icon name="school" color="primary" />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ prerequisiteTitleById[id] || 'Curso' }}</q-item-label>
                    <q-item-label caption>
                      Umbral de completacion:
                      <span class="text-weight-medium">{{ prerequisiteThresholds[id] ?? 80 }}%</span>
                    </q-item-label>
                  </q-item-section>
                  <q-item-section side style="min-width: 160px">
                    <q-slider
                      :model-value="prerequisiteThresholds[id] ?? 80"
                      :min="0" :max="100" :step="10"
                      label
                      label-always
                      color="primary"
                      @update:model-value="(v) => setThreshold(id, (v as number))"
                    />
                  </q-item-section>
                  <q-item-section side>
                    <q-btn flat dense round icon="close" color="negative" @click="removePrerequisite(id)" />
                  </q-item-section>
                </q-item>
              </q-list>
              <div v-else class="empty-hint">
                Sin prerequisitos. Este curso es accesible sin haber completado otros cursos.
              </div>

              <div class="panel-actions">
                <q-btn color="primary" unelevated no-caps label="Guardar prerequisitos" :loading="saving" @click="savePrerequisites" />
              </div>
            </template>
          </div>

          <!-- Seccion -->
          <div v-else-if="selected?.type === 'section'" class="panel">
            <div class="panel-header">
              <h2 class="panel-title q-my-none">Seccion</h2>
              <q-btn flat dense no-caps icon="delete" label="Eliminar" color="negative" @click="confirmDeleteSection" />
            </div>
            <q-input v-model="sectionForm.title" label="Titulo de la seccion" outlined />
            <q-input v-model="sectionForm.description" label="Descripcion" outlined type="textarea" rows="2" />

            <q-separator class="q-my-md" />

            <div class="row items-center justify-between q-mb-sm">
              <div class="text-subtitle2">Lecciones</div>
              <q-btn outline color="primary" no-caps icon="add" label="Añadir leccion" size="sm" @click="addLessonToCurrentSection" />
            </div>

            <q-list v-if="currentSection?.lessons?.length" separator bordered>
              <q-item v-for="l in currentSection.lessons" :key="l.id" clickable @click="selected = { type: 'lesson', id: l.id, sectionId: currentSection.id }">
                <q-item-section avatar><q-icon :name="lessonTypeIcon(l.type)" /></q-item-section>
                <q-item-section>
                  <q-item-label>{{ l.title }}</q-item-label>
                  <q-item-label caption>{{ lessonTypeLabel(l.type) }}<span v-if="l.isFree"> · Gratis (preview)</span></q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-icon name="chevron_right" />
                </q-item-section>
              </q-item>
            </q-list>
            <div v-else class="empty-hint">
              Esta seccion no tiene lecciones todavia.
            </div>

            <div class="panel-actions">
              <q-btn color="primary" unelevated no-caps label="Guardar seccion" :loading="saving" @click="saveSection" />
            </div>
          </div>

          <!-- Leccion -->
          <div v-else-if="selected?.type === 'lesson'" class="panel">
            <div class="panel-header">
              <h2 class="panel-title q-my-none">Leccion</h2>
              <q-btn flat dense no-caps icon="delete" label="Eliminar" color="negative" @click="confirmDeleteLesson" />
            </div>

            <q-input v-model="lessonForm.title" label="Titulo de la leccion" outlined />
            <q-input v-model="lessonForm.description" label="Descripcion breve (opcional)" outlined type="textarea" rows="2" />
            <q-toggle v-model="lessonForm.isFree" label="Leccion gratuita (preview publico)" />

            <div class="panel-actions" style="border-top: none; margin-top: 0; padding-top: 0">
              <q-btn color="primary" unelevated no-caps label="Guardar leccion" :loading="saving" @click="saveLesson" />
            </div>

            <q-separator class="q-my-md" />

            <!-- Blocks -->
            <div class="row items-center justify-between q-mb-sm">
              <div class="text-subtitle2">
                <q-icon name="view_agenda" size="18px" /> Contenido de la leccion ({{ blocks.length }})
              </div>
            </div>

            <p class="text-caption text-grey-7 q-mb-md" style="margin-top: -8px">
              Compone la leccion añadiendo bloques en el orden que quieras: texto, video, PDF, cuestionarios, proyectos o respuesta abierta.
            </p>

            <div v-if="loadingBlocks" class="text-center q-py-md">
              <q-spinner-dots color="primary" size="32px" />
            </div>

            <draggable
              v-else
              v-model="blocks"
              item-key="id"
              handle=".block-handle"
              animation="160"
              ghost-class="block-ghost"
              @end="handleBlockReorder"
            >
              <template #item="{ element: block }">
                <q-card flat bordered class="block-card q-mb-sm">
                  <div class="block-header">
                    <q-icon name="drag_indicator" class="block-handle" />
                    <q-icon :name="blockIcon(block.type)" :color="blockColor(block.type)" size="20px" class="q-mr-sm" />
                    <span class="text-weight-medium">{{ blockLabel(block.type) }}</span>
                    <q-space />
                    <q-btn flat dense round icon="delete" color="negative" size="sm" @click="confirmDeleteBlock(block)">
                      <q-tooltip>Eliminar bloque</q-tooltip>
                    </q-btn>
                  </div>
                  <q-card-section class="q-pt-none">
                    <!-- Text block -->
                    <div v-if="block.type === 'text'">
                      <RichTextEditor
                        v-model="block.textContent"
                        placeholder="Escribe el contenido..."
                      />
                      <div class="row justify-end q-mt-sm">
                        <q-btn color="primary" unelevated no-caps size="sm" label="Guardar bloque" :loading="block._saving" @click="saveBlock(block)" />
                      </div>
                    </div>

                    <!-- Video / PDF block -->
                    <div v-else-if="block.type === 'video' || block.type === 'pdf'">
                      <div v-if="block.type === 'video' ? block.videoUrl : block.pdfUrl" class="content-file-preview">
                        <q-icon :name="block.type === 'video' ? 'play_circle' : 'picture_as_pdf'" size="24px" color="primary" />
                        <span class="text-body2">Archivo subido correctamente</span>
                        <q-btn
                          flat
                          dense
                          icon="open_in_new"
                          size="sm"
                          @click="openFile(block.type === 'video' ? block.videoUrl! : block.pdfUrl!)"
                        />
                      </div>
                      <FileUploader
                        :accept="block.type === 'video' ? 'video/mp4' : 'application/pdf'"
                        :max-size-mb="block.type === 'video' ? 500 : 50"
                        :label="(block.type === 'video' ? block.videoUrl : block.pdfUrl) ? 'Reemplazar archivo' : 'Subir archivo'"
                        :hint="block.type === 'video' ? 'MP4 hasta 500 MB' : 'PDF hasta 50 MB'"
                        :icon="block.type === 'video' ? 'video_file' : 'picture_as_pdf'"
                        @upload="(file: File, onProgress: (pct: number) => void) => handleBlockFileUpload(block, file, onProgress)"
                      />
                    </div>

                    <!-- Assessment block (quiz / project / open_text) -->
                    <AssessmentEditor
                      v-else-if="isAssessmentType(block.type)"
                      :key="`assessment-${block.id}`"
                      :block-id="block.id"
                      :type="block.type"
                      :can-use-premium-features="canUsePremiumFeatures"
                    />
                  </q-card-section>
                </q-card>
              </template>
            </draggable>

            <q-btn-dropdown
              flat
              no-caps
              color="primary"
              icon="add"
              label="Añadir bloque"
              class="full-width q-mt-sm"
              :disable="addingBlock"
            >
              <q-list>
                <q-item v-for="opt in blockTypeOptions" :key="opt.value" clickable v-close-popup @click="addBlock(opt.value)">
                  <q-item-section avatar><q-icon :name="opt.icon" :color="opt.color" /></q-item-section>
                  <q-item-section>
                    <q-item-label>{{ opt.label }}</q-item-label>
                    <q-item-label caption>{{ opt.hint }}</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-btn-dropdown>

            <q-separator class="q-my-md" />

            <!-- Resources -->
            <div>
              <div class="row items-center justify-between q-mb-sm">
                <div class="text-subtitle2">
                  <q-icon name="attach_file" size="18px" /> Recursos descargables
                </div>
                <div class="text-caption text-grey-6">
                  {{ lessonResources.length }} archivo{{ lessonResources.length !== 1 ? 's' : '' }}
                </div>
              </div>

              <q-list v-if="lessonResources.length > 0" separator bordered class="q-mb-md">
                <q-item v-for="r in lessonResources" :key="r.id">
                  <q-item-section avatar>
                    <q-icon :name="resourceIcon(r.mimeType)" :color="resourceColor(r.mimeType)" size="28px" />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ r.fileName }}</q-item-label>
                    <q-item-label caption>
                      {{ formatFileSize(r.fileSize) }}
                      <span v-if="r.downloadCount > 0"> · {{ r.downloadCount }} descargas</span>
                    </q-item-label>
                  </q-item-section>
                  <q-item-section side>
                    <q-btn flat dense round icon="delete" color="negative" @click.stop="handleDeleteResource(r.id)" />
                  </q-item-section>
                </q-item>
              </q-list>

              <FileUploader
                ref="resourceUploaderRef"
                :max-size-mb="50"
                label="Añadir recurso descargable"
                hint="PDF, imagen, ZIP, DOCX, XLSX, TXT..."
                icon="upload_file"
                button-label="Elegir archivo"
                @upload="handleResourceUpload"
              />
            </div>
          </div>

          <!-- Empty state -->
          <div v-else class="panel empty-panel">
            <q-icon name="edit" size="48px" color="grey-4" />
            <div class="text-h6 text-grey-6 q-mt-md">Selecciona un elemento para editar</div>
            <p class="text-body2 text-grey-5">
              Usa la barra lateral para navegar entre la informacion del curso, sus secciones y lecciones.
            </p>
          </div>
        </main>
      </div>
    </template>

    <!-- Add section dialog -->
    <q-dialog v-model="showAddSection" persistent>
      <q-card style="min-width: 380px">
        <q-card-section>
          <div class="text-h6">Nueva seccion</div>
        </q-card-section>
        <q-card-section>
          <q-input v-model="newSectionTitle" label="Titulo" outlined autofocus @keyup.enter="createSectionNow" />
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn color="primary" label="Crear" unelevated :disable="!newSectionTitle" @click="createSectionNow" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Submit review dialog -->
    <q-dialog v-model="submitDialog">
      <q-card style="min-width: 420px">
        <q-card-section>
          <div class="text-h6">Enviar a revision</div>
        </q-card-section>
        <q-card-section>
          <div class="text-body2 q-mb-md">
            Si un administrador lo aprueba, tu curso se publicara automaticamente.
            Verifica que cumple estos requisitos:
          </div>
          <q-list dense>
            <q-item v-for="c in submitChecks" :key="c.label">
              <q-item-section avatar>
                <q-icon :name="c.ok ? 'check_circle' : 'cancel'" :color="c.ok ? 'positive' : 'negative'" />
              </q-item-section>
              <q-item-section>{{ c.label }}</q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn color="primary" label="Enviar" :disable="!allChecksPass" :loading="submitting" @click="doSubmitReview" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Delete confirm -->
    <q-dialog v-model="deleteDialog">
      <q-card style="min-width: 360px">
        <q-card-section>
          <div class="text-h6">{{ deleteTitle }}</div>
        </q-card-section>
        <q-card-section>{{ deleteMessage }}</q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn color="negative" unelevated label="Eliminar" :loading="deleting" @click="doDelete" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useQuasar } from 'quasar'
import {
  updateCourse, getCourseForEdit, submitForReview,
} from '../../api/instructor'
import { getCategories } from '../../api/course'
import { getCourseSections } from '../../api/lesson'
import { createSection, updateSection, deleteSection } from '../../api/sectionEditor'
import { createLesson, updateLesson, deleteLesson } from '../../api/lessonEditor'
import {
  listResources, uploadResource, uploadCourseThumbnail, uploadLessonContent,
  deleteResource as deleteResourceApi, formatFileSize, resourceIcon, resourceColor,
} from '../../api/resources'
import type { LessonResource } from '../../api/resources'
import RichTextEditor from '../../components/RichTextEditor.vue'
import FileUploader from '../../components/FileUploader.vue'
import AssessmentEditor from '../../components/AssessmentEditor.vue'
import draggable from 'vuedraggable'
import { useAuthStore } from '../../stores/auth'

function isAssessmentType(type: string): boolean {
  return type === 'quiz' || type === 'project' || type === 'open_text'
}
import { formatFileSize as fmtBytes } from '../../api/resources'
import { searchCourses } from '@/api/courseSearch'
import { getCoursePrerequisites, syncCoursePrerequisites } from '@/api/prerequisite'
import { checkIsPremium } from '@/api/payments'
import {
  listBlocks, createBlock, updateBlock, deleteBlock as deleteBlockApi, reorderBlocks,
} from '@/api/lessonBlock'
import type { LessonBlock, BlockType } from '@/types/lesson'

type EditableBlock = LessonBlock & { _saving?: boolean }

type Selected =
  | { type: 'info' }
  | { type: 'details' }
  | { type: 'prerequisites' }
  | { type: 'section'; id: string }
  | { type: 'lesson'; id: string; sectionId: string }
  | null

const route = useRoute()
const router = useRouter()
const $q = useQuasar()
const authStore = useAuthStore()

// Can this user use Premium-only features? Checked against backend (active subscription or admin role)
const canUsePremiumFeatures = ref(false)

const courseId = computed(() => route.params.id as string)
const course = ref<any>(null)
const sections = ref<any[]>([])
const loadingCourse = ref(true)
const saving = ref(false)

const selected = ref<Selected>({ type: 'info' })

// Options
const categoryOptions = ref<{ label: string; value: number }[]>([])
const levelOptions = [
  { label: 'Principiante', value: 'beginner' },
  { label: 'Intermedio', value: 'intermediate' },
  { label: 'Avanzado', value: 'advanced' },
]
const blockTypeOptions: { value: BlockType; label: string; icon: string; color: string; hint: string }[] = [
  { value: 'text', label: 'Texto', icon: 'article', color: 'primary', hint: 'Bloque de texto enriquecido' },
  { value: 'video', label: 'Video', icon: 'play_circle', color: 'primary', hint: 'Subir un MP4' },
  { value: 'pdf', label: 'PDF', icon: 'picture_as_pdf', color: 'red-7', hint: 'Subir un PDF' },
  { value: 'quiz', label: 'Cuestionario', icon: 'quiz', color: 'primary', hint: 'Preguntas de respuesta multiple' },
  { value: 'project', label: 'Proyecto', icon: 'upload_file', color: 'accent', hint: 'Entrega de archivo a calificar' },
  { value: 'open_text', label: 'Respuesta abierta', icon: 'edit_note', color: 'deep-purple', hint: 'Respuesta de texto a calificar' },
]

// Forms
const info = reactive({ title: '', slug: '', shortDescription: '', categoryId: null as number | null, level: 'beginner' })
const details = reactive({ description: '', thumbnailUrl: '', isPremium: false })
const sectionForm = reactive({ title: '', description: '' })
const lessonForm = reactive({
  title: '', description: '', isFree: false,
})
const lessonResources = ref<LessonResource[]>([])

// Blocks
const blocks = ref<EditableBlock[]>([])
const loadingBlocks = ref(false)
const addingBlock = ref(false)

// Prerequisites
const prerequisiteIds = ref<string[]>([])
const prerequisiteThresholds = reactive<Record<string, number>>({})
const prerequisiteTitleById = reactive<Record<string, string>>({})
const allPrereqCourses = ref<{ label: string; value: string }[]>([])
const prerequisiteOptions = ref<{ label: string; value: string }[]>([])
const loadingPrereqOptions = ref(false)

function setThreshold(id: string, value: number) {
  prerequisiteThresholds[id] = value
}

function removePrerequisite(id: string) {
  prerequisiteIds.value = prerequisiteIds.value.filter(x => x !== id)
  delete prerequisiteThresholds[id]
}

async function loadAllCoursesForPicker() {
  loadingPrereqOptions.value = true
  try {
    const page = await searchCourses({ page: 0, size: 200, sortBy: 'recent' })
    allPrereqCourses.value = (page.content || [])
      .filter((c: any) => c.id && c.id !== courseId.value)
      .map((c: any) => ({ label: c.title, value: c.id }))
    // Keep title cache for rendering existing selections
    for (const opt of allPrereqCourses.value) {
      prerequisiteTitleById[opt.value] = opt.label
    }
    prerequisiteOptions.value = allPrereqCourses.value
  } finally {
    loadingPrereqOptions.value = false
  }
}

function filterPrerequisiteOptions(val: string, update: (fn: () => void) => void) {
  update(() => {
    const needle = val.toLowerCase().trim()
    if (!needle) {
      prerequisiteOptions.value = allPrereqCourses.value
    } else {
      prerequisiteOptions.value = allPrereqCourses.value.filter(o => o.label.toLowerCase().includes(needle))
    }
  })
}

async function loadExistingPrerequisites() {
  if (!courseId.value) return
  try {
    const list = await getCoursePrerequisites(courseId.value)
    prerequisiteIds.value = list.map(p => p.prerequisiteCourseId)
    for (const p of list) {
      prerequisiteThresholds[p.prerequisiteCourseId] = p.completionThreshold ?? 80
      prerequisiteTitleById[p.prerequisiteCourseId] = p.prerequisiteTitle
    }
  } catch {
    // ignore
  }
}

async function savePrerequisites() {
  saving.value = true
  try {
    await syncCoursePrerequisites(courseId.value, {
      prerequisites: prerequisiteIds.value.map(id => ({
        prerequisiteCourseId: id,
        completionThreshold: prerequisiteThresholds[id] ?? 80,
      })),
    })
    $q.notify({ type: 'positive', message: 'Prerequisitos guardados', position: 'bottom-right' })
  } catch (err: any) {
    $q.notify({ type: 'negative', message: err?.response?.data?.message || 'Error al guardar prerequisitos', position: 'bottom-right' })
  } finally {
    saving.value = false
  }
}

// Storage limits per role (bytes)
const LIMIT_USER = 300 * 1024 * 1024          // 300 MB
const LIMIT_PREMIUM = 1024 * 1024 * 1024      // 1 GB

const storageLimit = computed<number>(() => {
  const role = authStore.user?.role || 'user'
  if (role === 'admin') return Infinity
  if (role === 'premium') return LIMIT_PREMIUM
  return LIMIT_USER
})

const isUnlimited = computed(() => storageLimit.value === Infinity)
const storageUsed = computed(() => course.value?.storageBytes || 0)

const storageRatio = computed(() => {
  if (isUnlimited.value) return 0
  return Math.min(1, storageUsed.value / storageLimit.value)
})

const storageLabel = computed(() => {
  if (isUnlimited.value) return `${fmtBytes(storageUsed.value)} (sin limite)`
  return `${fmtBytes(storageUsed.value)} / ${fmtBytes(storageLimit.value)}`
})

const storageColor = computed(() => {
  if (storageRatio.value >= 0.9) return 'negative'
  if (storageRatio.value >= 0.7) return 'warning'
  return 'primary'
})

// Uploader refs
const thumbnailUploaderRef = ref<InstanceType<typeof FileUploader> | null>(null)
const resourceUploaderRef = ref<InstanceType<typeof FileUploader> | null>(null)

// Section dialog
const showAddSection = ref(false)
const newSectionTitle = ref('')

// Submit review
const submitDialog = ref(false)
const submitting = ref(false)
const submitChecks = ref<{ label: string; ok: boolean }[]>([])
const allChecksPass = computed(() => submitChecks.value.every(c => c.ok))

// Delete
const deleteDialog = ref(false)
const deleteTitle = ref('')
const deleteMessage = ref('')
const deleting = ref(false)
let deleteFn: (() => Promise<void>) | null = null

const currentSection = computed(() => {
  const s = selected.value
  if (s?.type !== 'section') return null
  return sections.value.find(x => x.id === s.id) || null
})

const currentLesson = computed(() => {
  const s = selected.value
  if (s?.type !== 'lesson') return null
  const sec = sections.value.find(x => x.id === s.sectionId)
  return sec?.lessons.find((l: any) => l.id === s.id) || null
})

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

function statusColor(s: string) {
  switch (s) {
    case 'published': return 'positive'
    case 'pending_review': return 'warning'
    case 'rejected': return 'negative'
    default: return 'grey'
  }
}

function statusLabel(s: string) {
  switch (s) {
    case 'published': return 'Publicado'
    case 'pending_review': return 'En revision'
    case 'rejected': return 'Rechazado'
    default: return 'Borrador'
  }
}

function openPreview() {
  if (course.value?.slug) window.open(`/cursos/${course.value.slug}`, '_blank')
}

function openFile(url: string) {
  window.open(url, '_blank')
}

// --- Load ---

async function loadData() {
  loadingCourse.value = true
  try {
    // El curso es lo critico — si esto falla abortamos
    const c = await getCourseForEdit(courseId.value)
    course.value = c

    info.title = c.title
    info.slug = c.slug
    info.shortDescription = c.shortDescription || ''
    info.categoryId = c.categoryId || null
    info.level = c.level || 'beginner'

    details.description = c.description || ''
    details.thumbnailUrl = c.thumbnailUrl || ''
    details.isPremium = c.isFree === false

    // Premium check (for enabling/disabling premium-only features)
    canUsePremiumFeatures.value = await checkIsPremium().catch(() => false)

    // Auxiliares — cada uno atrapa su propio error
    const [cats, secs] = await Promise.all([
      getCategories().catch(err => { console.error('[loadData] getCategories failed:', err); return [] }),
      getCourseSections(courseId.value).catch(err => { console.error('[loadData] getCourseSections failed:', err); return [] }),
    ])
    categoryOptions.value = (cats as any[]).map((cat: any) => ({ label: cat.name, value: cat.id }))
    sections.value = secs as any[]

    // Prerequisitos (opcional, falla silenciosamente)
    await Promise.all([
      loadAllCoursesForPicker().catch(err => console.error('[loadData] loadAllCoursesForPicker failed:', err)),
      loadExistingPrerequisites().catch(err => console.error('[loadData] loadExistingPrerequisites failed:', err)),
    ])
  } catch (err: any) {
    console.error('[loadData] Failed to load course:', err)
    const msg = err?.response?.data?.message
      || err?.response?.statusText
      || err?.message
      || 'Error al cargar el curso'
    $q.notify({ type: 'negative', message: `${msg} (${err?.response?.status || '?'})`, position: 'bottom-right' })
    router.push('/instructor/cursos')
  } finally {
    loadingCourse.value = false
  }
}

watch(selected, async (s) => {
  if (s?.type === 'section' && currentSection.value) {
    sectionForm.title = currentSection.value.title || ''
    sectionForm.description = currentSection.value.description || ''
  }
  if (s?.type === 'lesson' && currentLesson.value) {
    lessonForm.title = currentLesson.value.title || ''
    lessonForm.description = currentLesson.value.description || ''
    lessonForm.isFree = currentLesson.value.isFree || false
    try {
      lessonResources.value = await listResources(currentLesson.value.id)
    } catch {
      lessonResources.value = []
    }
    await loadBlocks(currentLesson.value.id)
  }
})

async function saveInfo() {
  saving.value = true
  try {
    const updated = await updateCourse(courseId.value, {
      title: info.title,
      slug: info.slug,
      description: course.value.description || '',
      shortDescription: info.shortDescription || undefined,
      categoryId: info.categoryId || undefined,
      level: info.level,
    })
    course.value = { ...course.value, ...updated }
    $q.notify({ type: 'positive', message: 'Guardado', position: 'bottom-right' })
  } catch (err: any) {
    $q.notify({ type: 'negative', message: err?.response?.data?.message || 'Error al guardar', position: 'bottom-right' })
  } finally {
    saving.value = false
  }
}

async function saveDetails() {
  saving.value = true
  try {
    const updated = await updateCourse(courseId.value, {
      title: course.value.title,
      slug: course.value.slug,
      description: details.description,
      thumbnailUrl: details.thumbnailUrl || undefined,
      isFree: !details.isPremium,
    })
    course.value = { ...course.value, ...updated }
    $q.notify({ type: 'positive', message: 'Guardado', position: 'bottom-right' })
  } catch (err: any) {
    $q.notify({ type: 'negative', message: err?.response?.data?.message || 'Error al guardar', position: 'bottom-right' })
  } finally {
    saving.value = false
  }
}

async function saveSection() {
  if (!currentSection.value) return
  saving.value = true
  try {
    await updateSection(currentSection.value.id, {
      title: sectionForm.title,
      description: sectionForm.description,
      position: currentSection.value.position,
    })
    currentSection.value.title = sectionForm.title
    currentSection.value.description = sectionForm.description
    $q.notify({ type: 'positive', message: 'Seccion guardada', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al guardar seccion', position: 'bottom-right' })
  } finally {
    saving.value = false
  }
}

async function saveLesson() {
  if (!currentLesson.value) return
  saving.value = true
  try {
    const payload = {
      title: lessonForm.title,
      description: lessonForm.description || undefined,
      isFree: lessonForm.isFree,
    }
    const updated = await updateLesson(currentLesson.value.id, payload)
    Object.assign(currentLesson.value, updated)
    $q.notify({ type: 'positive', message: 'Leccion guardada', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al guardar leccion', position: 'bottom-right' })
  } finally {
    saving.value = false
  }
}

// --- Blocks ---

async function loadBlocks(lessonId: string) {
  loadingBlocks.value = true
  try {
    const list = await listBlocks(lessonId)
    blocks.value = list.map(b => ({ ...b }))
  } catch {
    blocks.value = []
  } finally {
    loadingBlocks.value = false
  }
}

function blockIcon(type: string) {
  const opt = blockTypeOptions.find(o => o.value === type)
  return opt?.icon || 'view_agenda'
}
function blockColor(type: string) {
  const opt = blockTypeOptions.find(o => o.value === type)
  return opt?.color || 'grey-7'
}
function blockLabel(type: string) {
  const opt = blockTypeOptions.find(o => o.value === type)
  return opt?.label || 'Bloque'
}

async function addBlock(type: BlockType) {
  if (!currentLesson.value) return
  addingBlock.value = true
  try {
    const created = await createBlock(currentLesson.value.id, {
      type,
      position: blocks.value.length,
    })
    blocks.value.push({ ...created })
  } catch (err: any) {
    $q.notify({
      type: 'negative',
      message: err?.response?.data?.message || 'Error al añadir bloque',
      position: 'bottom-right',
    })
  } finally {
    addingBlock.value = false
  }
}

async function saveBlock(block: EditableBlock) {
  block._saving = true
  try {
    const updated = await updateBlock(block.id, {
      textContent: block.textContent,
      videoUrl: block.videoUrl,
      pdfUrl: block.pdfUrl,
      position: block.position,
    })
    Object.assign(block, updated)
    $q.notify({ type: 'positive', message: 'Bloque guardado', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al guardar bloque', position: 'bottom-right' })
  } finally {
    block._saving = false
  }
}

function confirmDeleteBlock(block: EditableBlock) {
  deleteTitle.value = 'Eliminar bloque'
  deleteMessage.value = `¿Eliminar este bloque (${blockLabel(block.type)})? Si tiene una evaluacion asociada, tambien se eliminara.`
  deleteFn = async () => {
    await deleteBlockApi(block.id)
    blocks.value = blocks.value.filter(b => b.id !== block.id)
  }
  deleteDialog.value = true
}

async function handleBlockReorder() {
  if (!currentLesson.value) return
  blocks.value.forEach((b, i) => { b.position = i })
  try {
    await reorderBlocks(currentLesson.value.id, blocks.value.map(b => b.id))
  } catch {
    $q.notify({ type: 'negative', message: 'Error al reordenar', position: 'bottom-right' })
  }
}

async function handleBlockFileUpload(block: EditableBlock, file: File, onProgress: (pct: number) => void) {
  if (!currentLesson.value) return
  try {
    const url = await uploadLessonContent(currentLesson.value.id, file, onProgress)
    if (block.type === 'video') block.videoUrl = url
    else if (block.type === 'pdf') block.pdfUrl = url
    await saveBlock(block)
  } catch (err: any) {
    $q.notify({
      type: 'negative',
      message: err?.response?.data?.message || 'Error al subir archivo',
      position: 'bottom-right',
    })
  }
}

async function createSectionNow() {
  if (!newSectionTitle.value) return
  try {
    const created = await createSection(courseId.value, {
      title: newSectionTitle.value,
      position: sections.value.length,
    })
    created.lessons = []
    sections.value.push(created)
    showAddSection.value = false
    newSectionTitle.value = ''
    selected.value = { type: 'section', id: created.id }
  } catch {
    $q.notify({ type: 'negative', message: 'Error al crear seccion', position: 'bottom-right' })
  }
}

async function addLessonToCurrentSection() {
  if (!currentSection.value) return
  try {
    const created = await createLesson(currentSection.value.id, {
      title: 'Nueva leccion',
      type: 'text',
      position: currentSection.value.lessons.length,
    })
    currentSection.value.lessons.push(created)
    selected.value = { type: 'lesson', id: created.id, sectionId: currentSection.value.id }
  } catch {
    $q.notify({ type: 'negative', message: 'Error al crear leccion', position: 'bottom-right' })
  }
}

function confirmDeleteSection() {
  if (!currentSection.value) return
  deleteTitle.value = 'Eliminar seccion'
  deleteMessage.value = `¿Eliminar "${currentSection.value.title}" y todas sus lecciones?`
  deleteFn = async () => {
    await deleteSection(currentSection.value!.id)
    sections.value = sections.value.filter(s => s.id !== currentSection.value!.id)
    selected.value = { type: 'info' }
  }
  deleteDialog.value = true
}

function confirmDeleteLesson() {
  if (!currentLesson.value) return
  deleteTitle.value = 'Eliminar leccion'
  deleteMessage.value = `¿Eliminar "${currentLesson.value.title}"?`
  deleteFn = async () => {
    await deleteLesson(currentLesson.value!.id)
    const sec = sections.value.find(s => s.id === (selected.value as any).sectionId)
    if (sec) sec.lessons = sec.lessons.filter((l: any) => l.id !== currentLesson.value!.id)
    selected.value = { type: 'section', id: (selected.value as any).sectionId }
  }
  deleteDialog.value = true
}

async function doDelete() {
  if (!deleteFn) return
  deleting.value = true
  try {
    await deleteFn()
    deleteDialog.value = false
    $q.notify({ type: 'positive', message: 'Eliminado', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al eliminar', position: 'bottom-right' })
  } finally {
    deleting.value = false
  }
}

async function handleThumbnailUpload(file: File, onProgress: (pct: number) => void) {
  try {
    const url = await uploadCourseThumbnail(courseId.value, file, onProgress)
    details.thumbnailUrl = url
    thumbnailUploaderRef.value?.finish()
    $q.notify({ type: 'positive', message: 'Thumbnail subido', position: 'bottom-right' })
    await saveDetails()
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Error al subir'
    thumbnailUploaderRef.value?.finish(msg)
  }
}

async function handleResourceUpload(file: File, onProgress: (pct: number) => void) {
  if (!currentLesson.value) return
  try {
    const resource = await uploadResource(currentLesson.value.id, file, onProgress)
    lessonResources.value.push(resource)
    resourceUploaderRef.value?.finish()
    $q.notify({ type: 'positive', message: 'Recurso subido', position: 'bottom-right' })
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Error al subir'
    resourceUploaderRef.value?.finish(msg)
  }
}

async function handleDeleteResource(id: string) {
  try {
    await deleteResourceApi(id)
    lessonResources.value = lessonResources.value.filter(r => r.id !== id)
    $q.notify({ type: 'positive', message: 'Recurso eliminado', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al eliminar', position: 'bottom-right' })
  }
}

function openSubmitDialog() {
  const hasTitle = !!course.value?.title && course.value.title.length >= 3
  const hasDescription = !!course.value?.description && course.value.description.replace(/<[^>]*>/g, '').trim().length >= 20
  const hasCategory = !!course.value?.categoryId
  const hasSection = sections.value.length > 0
  const hasLesson = sections.value.some(s => s.lessons && s.lessons.length > 0)

  submitChecks.value = [
    { label: 'El curso tiene titulo', ok: hasTitle },
    { label: 'Descripcion completa (al menos 20 caracteres)', ok: hasDescription },
    { label: 'Categoria seleccionada', ok: hasCategory },
    { label: `Al menos 1 seccion (${sections.value.length})`, ok: hasSection },
    { label: `Al menos 1 leccion en alguna seccion`, ok: hasLesson },
  ]
  submitDialog.value = true
}

async function doSubmitReview() {
  submitting.value = true
  try {
    await submitForReview(courseId.value)
    course.value.status = 'pending_review'
    submitDialog.value = false
    $q.notify({ type: 'positive', message: 'Curso enviado a revision', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al enviar a revision', position: 'bottom-right' })
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.edit-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 50px);
  background: #f9fafb;
}

.edit-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.edit-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.edit-sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e5e7eb;
  overflow-y: auto;
  flex-shrink: 0;
}

.lesson-item {
  padding-left: 36px !important;
}

.edit-sidebar :deep(.active-item) {
  background: rgba(15, 118, 110, 0.08);
  color: #0f766e;
  font-weight: 500;
}

.storage-panel {
  padding: 12px 14px;
  margin: 8px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.edit-main {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.panel {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  border: 1px solid #e5e7eb;
}

.panel-title {
  font-family: 'Monda', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 8px;
  color: #0f172a;
}

.panel-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
  margin-top: 8px;
}

.empty-panel {
  text-align: center;
  padding: 64px 24px;
  border: 2px dashed #e5e7eb;
  background: transparent;
}

.empty-hint {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 0.875rem;
  text-align: center;
}

.premium-lock {
  text-align: center;
  padding: 40px 24px;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border: 1px dashed #d97706;
  border-radius: 12px;
}

.thumbnail-preview {
  position: relative;
  display: inline-block;
  margin-bottom: 8px;
}

.thumbnail-preview img {
  max-width: 280px;
  max-height: 160px;
  border-radius: 8px;
  display: block;
}

.thumbnail-preview .q-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  background: rgba(255, 255, 255, 0.9);
}

.content-file-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f0fdfa;
  border: 1px solid #99f6e4;
  border-radius: 8px;
  margin-bottom: 8px;
}

.block-card {
  border-radius: 10px;
  background: #fff;
}

.block-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}

.block-handle {
  cursor: grab;
  color: #94a3b8;
}

.block-handle:active {
  cursor: grabbing;
}

.block-ghost {
  opacity: 0.4;
  background: #ecfeff;
}
</style>
