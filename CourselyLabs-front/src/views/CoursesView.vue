<template>
  <q-page class="q-pa-md q-pa-lg-lg">
    <div class="content-wrap">
      <h4 class="q-mt-none q-mb-md">Cursos</h4>

      <!-- Bloque nuevo para los filtros, insertado sin romper la estructura existente -->
      <div class="row q-col-gutter-md items-center q-mb-md">
        <div class="col-12 col-md-3">
          <q-input v-model="keyword" filled clearable label="Buscar cursos por título">
            <template #prepend>
              <q-icon name="search" />
            </template>
          </q-input>
        </div>

        <div class="col-4 col-md-3">
          <q-select
            v-model="categoryId"
            :options="categoryOptions"
            emit-value
            map-options
            clearable
            label="Categoría"
            filled
          />
        </div>

        <div class="col-4 col-md-3">
          <q-select
            v-model="isFree"
            :options="typeOptions"
            emit-value
            map-options
            clearable
            label="Tipo"
            filled
          />
        </div>

        <div class="col-4 col-md-3">
          <q-select
            v-model="level"
            :options="levelOptions"
            emit-value
            map-options
            clearable
            label="Level"
            filled
          />
        </div>

        <div class="col-4 col-md-3">
          <q-select
            v-model="sortBy"
            :options="sortOptions"
            emit-value
            map-options
            clearable
            label="Ordenar por"
            filled
          />
        </div>
      </div>

      <div class="row items-center justify-between q-mb-md">
        <div class="text-subtitle1">{{ totalElements }} cursos encontrados</div>
      </div>

      <div v-if="hasActiveFilters" class="q-mb-md row q-gutter-sm">
        <q-chip v-if="keyword" removable @remove="removeFilterChip('keyword')">{{ keyword }}</q-chip>
        <q-chip v-if="categoryId !== null" removable @remove="removeFilterChip('categoryId')">Categoría</q-chip>
        <q-chip v-if="level" removable @remove="removeFilterChip('level')">{{ level }}</q-chip>
        <q-chip v-if="isFree !== null" removable @remove="removeFilterChip('isFree')">
          {{ isFree ? 'Gratis' : 'Premium' }}
        </q-chip>
        <q-btn flat color="primary" label="Limpiar filtros" @click="clearAllFilters" />
      </div>

      <!-- Estructura original conservada -->
      <template v-if="loading">
        <div class="row q-col-gutter-md">
          <div v-for="n in 6" :key="n" class="col-12 col-sm-6 col-md-4">
            <q-card>
              <q-skeleton type="rect" height="160px" />
              <q-card-section>
                <q-skeleton type="text" width="70%" />
                <q-skeleton type="text" width="90%" class="q-mt-xs" />
              </q-card-section>
              <q-card-section class="q-pt-none">
                <q-skeleton type="QChip" width="60px" />
              </q-card-section>
            </q-card>
          </div>
        </div>
      </template>

      <q-banner v-else-if="error" rounded class="bg-red-1 text-negative q-mb-md" inline-actions>
        <template #avatar>
          <q-icon name="warning" />
        </template>
        {{ error }}
        <template #action>
          <q-btn flat color="negative" label="Reintentar" @click="fetchPage(true)" />
        </template>
      </q-banner>

      <template v-else>
        <div v-if="courses.length === 0" class="text-center q-pa-xl">
          <q-icon name="search" size="64px" color="grey-5" />
          <div class="text-h6 text-grey-7 q-mt-md">No hay cursos disponibles</div>
          <p class="text-grey-6">Vuelve pronto, estamos preparando nuevo contenido.</p>
        </div>

        <div v-else class="row q-col-gutter-md">
          <div v-for="course in courses" :key="course.id" class="col-12 col-sm-6 col-md-4">
            <CourseCard :course="course" />
          </div>
        </div>

        <div class="row justify-center q-mt-lg" v-if="hasNext">
          <q-btn outline color="primary" :loading="loadingMore" label="Cargar mas" @click="fetchPage(false)" />
        </div>
      </template>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import CourseCard from '@/components/CourseCard.vue'
import { useCourseSearch } from '@/composables/useCourseSearch'
import { getCategories } from '@/api/course'

const {
  courses,
  loading,
  loadingMore,
  error,
  totalElements,
  hasNext,
  keyword,
  categoryId,
  level,
  isFree,
  sortBy,
  hasActiveFilters,
  fetchPage,
  clearAllFilters,
  removeFilterChip
} = useCourseSearch()

// Opciones para el select de ordenamiento, nivel, tipo y rating
const sortOptions = [
  { label: 'Mas recientes', value: 'recent' },
  { label: 'Mas populares', value: 'popular' },
  { label: 'Mejor valorados', value: 'rating' },
  { label: 'Precio: menor a mayor', value: 'price_asc' },
  { label: 'Precio: mayor a menor', value: 'price_desc' }
]

// Opciones para el select de tipo (gratis/premium)
const typeOptions = [
  { label: 'Gratis', value: true },
  { label: 'Premium', value: false }
]

// Opciones para el select de nivel
const levelOptions = [
  { label: 'Beginner', value: 'beginner' },
  { label: 'Intermediate', value: 'intermediate' },
  { label: 'Advanced', value: 'advanced' }
]

// Opciones para el select de categorías, cargadas desde la API
const categoryOptions = ref<Array<{ label: string; value: number }>>([])

// Función para cargar categorías desde la API y mapearlas al formato requerido por q-select
async function loadCategories() {
  try {
    const categories = await getCategories()
    categoryOptions.value = Array.isArray(categories)
      ? categories.map((category: { id: number; name: string }) => ({
          label: category.name,
          value: category.id,
        }))
      : []
  } catch {
    categoryOptions.value = []
  }
}

// Watchers para recargar la página cada vez que cambie un filtro
watch([keyword, categoryId, level, isFree, sortBy], () => {
  fetchPage(true)
})

// Cargar categorías y la primera página al montar el componente
onMounted(async () => {
  await loadCategories()
  fetchPage(true)
})
</script>
