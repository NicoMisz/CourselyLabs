<template>
  <q-page class="q-pa-md q-pa-lg-lg">
    <div class="content-wrap">
      <h4 class="q-mt-none q-mb-md">Cursos</h4>

      <!-- Bloque nuevo para los filtros, insertado sin romper la estructura existente -->
      <div class="row q-col-gutter-md items-center q-mb-md">
        <div class="col-12 col-md-8">
          <q-input v-model="keyword" filled clearable label="Buscar cursos">
            <template #prepend>
              <q-icon name="search" />
            </template>
          </q-input>
        </div>
        <div class="col-12 col-md-4">
          <q-select
            v-model="sortBy"
            :options="sortOptions"
            emit-value
            map-options
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
        <q-chip v-if="level" removable @remove="removeFilterChip('level')">{{ level }}</q-chip>
        <q-chip v-if="isFree !== null" removable @remove="removeFilterChip('isFree')">
          {{ isFree ? 'Gratis' : 'Premium' }}
        </q-chip>
        <q-chip v-if="minRating" removable @remove="removeFilterChip('minRating')">
          {{ minRating }}★ y mas
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
          <q-btn flat color="negative" label="Reintentar" @click="fetchPage (true)" />
        </template>
      </q-banner>

      <template v-else>
        <div v-if="courses.length === 0" class="text-center q-pa-xl">
          <q-icon name="search" size="64px" color="grey-5" />
          <div class="text-h6 text-grey-7 q-mt-md">No hay cursos disponibles</div>
          <p class="text-grey-6">Vuelve pronto, estamos preparando nuevo contenido.</p>
<!--           <div class="text-h6 text-grey-7 q-mt-md">No se encontraron cursos</div>
          <p class="text-grey-6">Prueba otra busqueda o limpia filtros.</p>
          <q-btn color="primary" outline label="Limpiar filtros" @click="clearAllFilters" /> -->
        </div>

        <div v-else class="row q-col-gutter-md">
          <div v-for="course in courses" :key="course.id" class="col-12 col-sm-6 col-md-4">
            <CourseCard :course="course" />
          </div>
        </div>

        <!-- Fase 1: cargar mas conservador; fase 2: q-infinite-scroll -->
        <div class="row justify-center q-mt-lg" v-if="hasNext">
          <q-btn
            outline
            color="primary"
            :loading="loadingMore"
            label="Cargar mas"
            @click="fetchPage(false)"
          />
        </div>

      </template>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import CourseCard from '@/components/CourseCard.vue'
//import { useCourses } from '@/composables/useCourses'
import { useCourseSearch } from '@/composables/useCourseSearch'

//const { courses, loading, error, fetchCourses } = useCourses()

const {
  courses,
  loading,
  loadingMore,
  error,
  totalElements,
  hasNext,
  keyword,
  level,
  isFree,
  minRating,
  sortBy,
  hasActiveFilters,
  fetchPage,
  clearAllFilters,
  removeFilterChip
} = useCourseSearch()

const sortOptions = [
  { label: 'Mas recientes', value: 'recent' },
  { label: 'Mas populares', value: 'popular' },
  { label: 'Mejor valorados', value: 'rating' },
  { label: 'Precio: menor a mayor', value: 'price_asc' },
  { label: 'Precio: mayor a menor', value: 'price_desc' }
]

onMounted(() => {
  fetchPage(true)
})

//onMounted(fetchCourses)
</script>
