<template>
  <div style="padding: 2rem;">
    <h4>Cursos</h4>

    <q-spinner v-if="loading" size="3em" color="primary" />

    <q-banner v-else-if="error" class="bg-negative text-white q-mb-md">
      {{ error }}
    </q-banner>

    <div v-else class="row q-col-gutter-md">
      <div v-for="course in courses" :key="course.id" class="col-12 col-sm-6 col-md-4">
        <q-card class="cursor-pointer" @click="$router.push(`/cursos/${course.slug}`)">
          <q-card-section>
            <div class="text-h6">{{ course.title }}</div>
            <div class="text-subtitle2 text-grey">{{ course.level }}</div>
          </q-card-section>
          <q-card-section>
            <p>{{ course.shortDescription }}</p>
            <q-badge :color="course.isFree ? 'green' : 'orange'">
              {{ course.isFree ? 'Gratis' : `${course.price} €` }}
            </q-badge>
          </q-card-section>
        </q-card>
      </div>

      <div v-if="courses.length === 0" class="col-12">
        <p>No hay cursos disponibles.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

interface Course {
  id: string
  slug: string
  title: string
  shortDescription: string
  level: string
  isFree: boolean
  price: number
}

const courses = ref<Course[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/courses/all`)
    courses.value = data
  } catch (e) {
    error.value = 'Error al cargar los cursos. Asegúrate de que el backend esté corriendo.'
  } finally {
    loading.value = false
  }
})
</script>
