<template>
  <q-page class="q-pa-md q-pa-lg-lg">
    <div class="content-wrap">
      <h4 class="q-mt-none q-mb-md">Cursos</h4>

      <!-- Barra de búsqueda + filtros + orden -->
      <div class="search-bar q-mb-sm">
        <q-input
          v-model="keyword"
          filled
          clearable
          dense
          placeholder="Buscar cursos por título"
          class="search-bar__input"
        >
          <template #prepend>
            <q-icon name="search" />
          </template>
        </q-input>

        <q-btn-dropdown
          outline
          no-caps
          color="primary"
          icon="tune"
          :label="filtersButtonLabel"
          class="search-bar__filters"
        >
          <div class="filters-panel q-pa-md">
            <div class="text-caption text-weight-medium text-grey-7 q-mb-xs">CATEGORÍA</div>
            <q-select
              v-model="categoryId"
              :options="categoryOptions"
              emit-value
              map-options
              clearable
              dense
              outlined
              placeholder="Todas las categorías"
              class="q-mb-md"
            />

            <div class="text-caption text-weight-medium text-grey-7 q-mb-xs">NIVEL</div>
            <q-option-group
              v-model="level"
              :options="levelOptions"
              type="radio"
              color="primary"
              class="q-mb-md"
            />

            <div class="text-caption text-weight-medium text-grey-7 q-mb-xs">TIPO</div>
            <q-option-group
              v-model="isFree"
              :options="typeOptions"
              type="radio"
              color="primary"
              class="q-mb-md"
            />

            <div class="row justify-end q-mt-sm">
              <q-btn
                flat
                no-caps
                dense
                color="grey-7"
                label="Limpiar todo"
                :disable="!hasActiveFilters"
                @click="clearAllFilters"
              />
            </div>
          </div>
        </q-btn-dropdown>

        <q-select
          v-model="sortBy"
          :options="sortOptions"
          emit-value
          map-options
          dense
          outlined
          class="search-bar__sort"
        >
          <template #prepend>
            <q-icon name="sort" size="18px" />
          </template>
        </q-select>
      </div>

      <!-- Línea de resultados + chips de filtros activos -->
      <div class="row items-center justify-between q-mb-md q-gutter-sm">
        <div class="text-subtitle2 text-grey-7">
          {{ totalElements }} {{ totalElements === 1 ? 'curso encontrado' : 'cursos encontrados' }}
        </div>
        <div v-if="hasActiveFilters" class="row q-gutter-xs items-center">
          <q-chip
            v-if="keyword"
            removable
            color="primary"
            text-color="white"
            dense
            @remove="removeFilterChip('keyword')"
          >
            «{{ keyword }}»
          </q-chip>
          <q-chip
            v-if="categoryId !== null"
            removable
            color="primary"
            text-color="white"
            dense
            @remove="removeFilterChip('categoryId')"
          >
            {{ categoryLabel }}
          </q-chip>
          <q-chip
            v-if="level"
            removable
            color="primary"
            text-color="white"
            dense
            @remove="removeFilterChip('level')"
          >
            {{ levelLabel }}
          </q-chip>
          <q-chip
            v-if="isFree !== null"
            removable
            color="primary"
            text-color="white"
            dense
            @remove="removeFilterChip('isFree')"
          >
            {{ isFree ? 'Gratis' : 'Premium' }}
          </q-chip>
        </div>
      </div>

      <!-- Loading -->
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
          <div class="text-h6 text-grey-7 q-mt-md">No hemos encontrado cursos con esos filtros</div>
          <p class="text-grey-6">Prueba a quitar alguno o vuelve pronto, estamos preparando nuevo contenido.</p>
          <q-btn
            v-if="hasActiveFilters"
            outline
            color="primary"
            label="Limpiar filtros"
            no-caps
            @click="clearAllFilters"
          />
        </div>

        <div v-else class="row q-col-gutter-md">
          <div v-for="course in courses" :key="course.id" class="col-12 col-sm-6 col-md-4">
            <CourseCard :course="course" />
          </div>
        </div>

        <div class="row justify-center q-mt-lg" v-if="hasNext">
          <q-btn outline color="primary" :loading="loadingMore" label="Cargar más" no-caps @click="fetchPage(false)" />
        </div>
      </template>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
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
  removeFilterChip,
} = useCourseSearch()

const sortOptions = [
  { label: 'Más recientes', value: 'recent' },
  { label: 'Más populares', value: 'popular' },
  { label: 'Mejor valorados', value: 'rating' },
  { label: 'Precio: menor a mayor', value: 'price_asc' },
  { label: 'Precio: mayor a menor', value: 'price_desc' },
]

const typeOptions = [
  { label: 'Cualquiera', value: null },
  { label: 'Gratis', value: true },
  { label: 'Premium', value: false },
]

const levelOptions = [
  { label: 'Cualquiera', value: null },
  { label: 'Principiante', value: 'beginner' },
  { label: 'Intermedio', value: 'intermediate' },
  { label: 'Avanzado', value: 'advanced' },
]

const categoryOptions = ref<Array<{ label: string; value: number }>>([])

// Recuento de filtros activos para mostrar en el botón
const activeFilterCount = computed(() => {
  let n = 0
  if (categoryId.value !== null) n++
  if (level.value) n++
  if (isFree.value !== null) n++
  return n
})

const filtersButtonLabel = computed(() =>
  activeFilterCount.value === 0
    ? 'Filtros'
    : `Filtros (${activeFilterCount.value})`
)

// Labels resueltos para mostrar en chips
const categoryLabel = computed(() => {
  const opt = categoryOptions.value.find(o => o.value === categoryId.value)
  return opt?.label ?? 'Categoría'
})
const levelLabel = computed(() => {
  const opt = levelOptions.find(o => o.value === level.value)
  return opt?.label ?? 'Nivel'
})

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

watch([keyword, categoryId, level, isFree, sortBy], () => {
  fetchPage(true)
})

onMounted(async () => {
  await loadCategories()
  fetchPage(true)
})
</script>

<style scoped>
.content-wrap {
  max-width: 1200px;
  margin: 0 auto;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.search-bar__input {
  flex: 1 1 280px;
  min-width: 240px;
}

.search-bar__filters {
  flex: 0 0 auto;
  height: 40px;
}

.search-bar__sort {
  flex: 0 0 240px;
  min-width: 200px;
}

@media (max-width: 600px) {
  .search-bar__sort {
    flex: 1 1 100%;
  }
}

.filters-panel {
  min-width: 280px;
  max-width: 320px;
}
</style>
