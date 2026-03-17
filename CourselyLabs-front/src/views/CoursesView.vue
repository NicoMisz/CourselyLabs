<template>
  <q-page class="q-pa-md q-pa-lg-lg">
    <div class="content-wrap">
      <h4 class="q-mt-none q-mb-md">Cursos</h4>

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
          <q-btn flat color="negative" label="Reintentar" @click="fetchCourses" />
        </template>
      </q-banner>

      <template v-else>
        <div v-if="courses.length === 0" class="text-center q-pa-xl">
          <q-icon name="sym_o_school" size="64px" color="grey-5" />
          <div class="text-h6 text-grey-7 q-mt-md">No hay cursos disponibles</div>
          <p class="text-grey-6">Vuelve pronto, estamos preparando nuevo contenido.</p>
        </div>

        <div v-else class="row q-col-gutter-md">
          <div v-for="course in courses" :key="course.id" class="col-12 col-sm-6 col-md-4">
            <CourseCard :course="course" />
          </div>
        </div>
      </template>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import CourseCard from '@/components/CourseCard.vue'
import { useCourses } from '@/composables/useCourses'

const { courses, loading, error, fetchCourses } = useCourses()

onMounted(fetchCourses)
</script>
