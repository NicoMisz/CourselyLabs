<template>
  <q-page class="q-pa-lg">
    <div class="row items-center q-mb-lg">
      <h1 class="text-h5 q-ma-none col">Gestión de categorías</h1>
      <q-btn unelevated color="primary" icon="add" label="Nueva categoría" no-caps @click="openCreateDialog" />
    </div>

    <q-table
      :rows="categories"
      :columns="columns"
      row-key="id"
      :loading="loading"
      flat
      bordered
      :rows-per-page-options="[0]"
      hide-pagination
    >
      <template #body-cell-name="props">
        <q-td :props="props">
          <div class="text-body2 text-weight-medium">{{ props.row.name }}</div>
        </q-td>
      </template>

      <template #body-cell-slug="props">
        <q-td :props="props">
          <code class="text-caption">{{ props.row.slug }}</code>
        </q-td>
      </template>

      <template #body-cell-description="props">
        <q-td :props="props" class="description-cell">
          {{ props.row.description || '—' }}
        </q-td>
      </template>

      <template #body-cell-actions="props">
        <q-td :props="props" class="text-right">
          <q-btn flat dense round icon="edit" color="primary" @click="openEditDialog(props.row)">
            <q-tooltip>Editar</q-tooltip>
          </q-btn>
          <q-btn flat dense round icon="delete" color="negative" @click="confirmDelete(props.row)">
            <q-tooltip>Borrar</q-tooltip>
          </q-btn>
        </q-td>
      </template>

      <template #no-data>
        <div class="full-width row flex-center q-pa-md text-grey-7">
          No hay categorías. Crea la primera con el botón de arriba.
        </div>
      </template>
    </q-table>

    <!-- Create/Edit dialog -->
    <q-dialog v-model="formDialog" persistent>
      <q-card style="min-width: 480px">
        <q-card-section>
          <div class="text-h6">{{ editingId === null ? 'Nueva categoría' : 'Editar categoría' }}</div>
        </q-card-section>

        <q-card-section class="q-gutter-md">
          <q-input
            v-model="form.name"
            label="Nombre"
            outlined
            dense
            :rules="[v => !!v || 'El nombre es obligatorio']"
            @update:model-value="onNameInput"
          />

          <q-input
            v-model="form.slug"
            label="Slug (URL)"
            outlined
            dense
            hint="Solo minúsculas, números y guiones. Sin tildes."
            :rules="[
              v => !!v || 'El slug es obligatorio',
              v => /^[a-z0-9-]+$/.test(v) || 'Solo minúsculas, números y guiones'
            ]"
          />

          <q-input
            v-model="form.description"
            label="Descripción (opcional)"
            outlined
            type="textarea"
            rows="3"
          />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancelar" no-caps v-close-popup :disable="saving" />
          <q-btn unelevated color="primary" :label="editingId === null ? 'Crear' : 'Guardar'" no-caps :loading="saving" @click="save" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Delete confirmation -->
    <q-dialog v-model="deleteDialog" persistent>
      <q-card style="min-width: 420px">
        <q-card-section>
          <div class="text-h6">Borrar categoría</div>
        </q-card-section>
        <q-card-section>
          ¿Seguro que quieres borrar <strong>«{{ deleteTarget?.name }}»</strong>?
          <p class="text-caption text-grey-7 q-mt-sm">
            La categoría no se podrá borrar si hay cursos asociados.
          </p>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" no-caps v-close-popup :disable="deleting" />
          <q-btn color="negative" label="Borrar" no-caps :loading="deleting" @click="doDelete" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useQuasar, type QTableColumn } from 'quasar'
import {
  listCategories,
  createCategory,
  updateCategory,
  deleteCategory,
  type Category,
} from '@/api/category'

const $q = useQuasar()

const categories = ref<Category[]>([])
const loading = ref(true)

const columns: QTableColumn[] = [
  { name: 'name', label: 'Nombre', field: 'name', align: 'left', sortable: true },
  { name: 'slug', label: 'Slug', field: 'slug', align: 'left', sortable: true },
  { name: 'description', label: 'Descripción', field: 'description', align: 'left' },
  { name: 'actions', label: 'Acciones', field: 'id', align: 'right' },
]

const formDialog = ref(false)
const editingId = ref<number | null>(null)
const slugManuallyEdited = ref(false)
const form = ref({ name: '', slug: '', description: '' })
const saving = ref(false)

const deleteDialog = ref(false)
const deleteTarget = ref<Category | null>(null)
const deleting = ref(false)

async function fetchCategories() {
  loading.value = true
  try {
    categories.value = await listCategories()
  } catch {
    $q.notify({ type: 'negative', message: 'Error al cargar las categorías', position: 'bottom-right' })
  } finally {
    loading.value = false
  }
}

function slugify(text: string): string {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

function onNameInput(value: string | number | null) {
  if (!slugManuallyEdited.value && editingId.value === null) {
    form.value.slug = slugify(String(value ?? ''))
  }
}

function openCreateDialog() {
  editingId.value = null
  slugManuallyEdited.value = false
  form.value = { name: '', slug: '', description: '' }
  formDialog.value = true
}

function openEditDialog(category: Category) {
  editingId.value = category.id
  slugManuallyEdited.value = true
  form.value = {
    name: category.name,
    slug: category.slug,
    description: category.description ?? '',
  }
  formDialog.value = true
}

async function save() {
  if (!form.value.name || !form.value.slug) return
  if (!/^[a-z0-9-]+$/.test(form.value.slug)) return

  saving.value = true
  try {
    const payload = {
      name: form.value.name,
      slug: form.value.slug,
      description: form.value.description || null,
    }
    if (editingId.value === null) {
      const created = await createCategory(payload)
      categories.value.push(created)
      $q.notify({ type: 'positive', message: `Categoría «${created.name}» creada`, position: 'bottom-right' })
    } else {
      const updated = await updateCategory(editingId.value, payload)
      const idx = categories.value.findIndex(c => c.id === updated.id)
      if (idx >= 0) categories.value[idx] = updated
      $q.notify({ type: 'positive', message: `Categoría «${updated.name}» actualizada`, position: 'bottom-right' })
    }
    formDialog.value = false
  } catch (err: unknown) {
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      ?? 'Error al guardar la categoría'
    $q.notify({ type: 'negative', message, position: 'bottom-right' })
  } finally {
    saving.value = false
  }
}

function confirmDelete(category: Category) {
  deleteTarget.value = category
  deleteDialog.value = true
}

async function doDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deleteCategory(deleteTarget.value.id)
    categories.value = categories.value.filter(c => c.id !== deleteTarget.value!.id)
    $q.notify({ type: 'positive', message: `Categoría «${deleteTarget.value.name}» borrada`, position: 'bottom-right' })
    deleteDialog.value = false
  } catch (err: unknown) {
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      ?? 'Error al borrar la categoría'
    $q.notify({ type: 'negative', message, position: 'bottom-right' })
  } finally {
    deleting.value = false
  }
}

onMounted(fetchCategories)
</script>

<style scoped>
.description-cell {
  max-width: 400px;
  white-space: normal;
}
</style>
