<template>
  <q-page>
    <!-- Hero -->
    <section class="hero q-pa-xl text-center">
      <div style="max-width: 700px; margin: 0 auto">
        <h1 class="text-h3 q-mt-none q-mb-sm hero-title">
          Aprende con CourselyLabs
        </h1>
        <p class="text-body1 q-mb-lg hero-subtitle">
          Cursos online para avanzar en tu carrera. Aprende a tu ritmo con instructores expertos.
        </p>
        <q-btn
          unelevated
          rounded
          color="accent"
          label="Explorar cursos"
          to="/cursos"
          size="lg"
          icon="school"
        />
      </div>
    </section>

    <div class="q-pa-md q-pa-lg-lg content-wrap">
      <!-- Continuar aprendiendo (si autenticado) -->
      <section v-if="authStore.isLoggedIn && lastEnrolledCourse" class="q-mb-xl">
        <h2 class="text-h5 q-mb-md" style="font-family: Monda, sans-serif">Continuar aprendiendo</h2>
        <div style="max-width: 380px">
          <CourseCardEnrolled :course="lastEnrolledCourse" />
        </div>
      </section>

      <!-- Cursos destacados -->
      <section class="q-mb-xl">
        <div class="row items-center q-mb-md">
          <h2 class="text-h5 q-my-none" style="font-family: Monda, sans-serif">Cursos destacados</h2>
          <q-space />
          <q-btn flat color="primary" label="Ver todos" to="/cursos" />
        </div>

        <div v-if="loadingCourses" class="row q-col-gutter-md">
          <div v-for="n in 4" :key="n" class="col-12 col-sm-6 col-md-3">
            <q-card>
              <q-skeleton type="rect" height="140px" />
              <q-card-section>
                <q-skeleton type="text" width="70%" />
                <q-skeleton type="text" width="90%" />
              </q-card-section>
            </q-card>
          </div>
        </div>

        <div v-else class="row q-col-gutter-md">
          <div v-for="course in featuredCourses" :key="course.id" class="col-12 col-sm-6 col-md-3">
            <CourseCard :course="course" />
          </div>
        </div>
      </section>

      <!-- Categorías -->
      <section v-if="categories.length" class="q-mb-xl">
        <h2 class="text-h5 q-mb-md" style="font-family: Monda, sans-serif">Categorías</h2>
        <div class="row q-gutter-sm">
          <q-chip
            v-for="cat in categories"
            :key="cat.id"
            clickable
            outline
            color="primary"
            :label="cat.name"
            @click="$router.push(`/cursos?cat=${cat.id}`)"
          />
        </div>
      </section>

      <!-- CTA Instructor -->
      <section class="instructor-cta q-pa-lg rounded-borders text-center q-mb-lg">
        <h2 class="text-h5 q-mt-none q-mb-sm" style="font-family: Monda, sans-serif">
          ¿Quieres enseñar?
        </h2>
        <p class="text-body1 text-grey-7 q-mb-md">
          Comparte tu conocimiento con miles de estudiantes. Crea tu primer curso hoy.
        </p>
        <q-btn unelevated rounded color="accent" label="Empieza aquí" icon="co_present" to="/instructor" />
      </section>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '@/api/axios'
import { useAuthStore } from '@/stores/auth'
import CourseCard from '@/components/CourseCard.vue'
import CourseCardEnrolled from '@/components/CourseCardEnrolled.vue'
import { getMyCourses } from '@/api/enrollment'
import type { Course } from '@/types/course'
import type { EnrolledCourse } from '@/types/enrollment'

interface Category {
  id: number
  name: string
  slug: string
}

const authStore = useAuthStore()
const allCourses = ref<Course[]>([])
const categories = ref<Category[]>([])
const lastEnrolledCourse = ref<EnrolledCourse | null>(null)
const loadingCourses = ref(true)

const featuredCourses = computed(() => allCourses.value.slice(0, 4))

onMounted(async () => {
  const promises: Promise<void>[] = []

  promises.push(
    api.get<Course[]>('/api/courses/all')
      .then(({ data }) => { allCourses.value = data })
      .catch(() => { /* silently fail */ })
      .finally(() => { loadingCourses.value = false })
  )

  promises.push(
    api.get<Category[]>('/api/categories')
      .then(({ data }) => { categories.value = data })
      .catch(() => { /* silently fail */ })
  )

  if (authStore.isLoggedIn) {
    promises.push(
      getMyCourses()
        .then((data) => {
          const sorted = data.sort((a, b) => {
            const aTime = a.lastAccessedAt ? new Date(a.lastAccessedAt).getTime() : 0
            const bTime = b.lastAccessedAt ? new Date(b.lastAccessedAt).getTime() : 0
            return bTime - aTime
          })
          lastEnrolledCourse.value = sorted[0] || null
        })
        .catch(() => { /* endpoint may not exist yet */ })
    )
  }

  await Promise.all(promises)
})
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.08), rgba(234, 88, 12, 0.05));
  padding-top: 4rem;
  padding-bottom: 4rem;
}
:global(.body--dark) .hero {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.18), rgba(234, 88, 12, 0.12));
}
.hero-title {
  font-family: Monda, sans-serif;
  color: var(--app-text-strong);
}
.hero-subtitle {
  color: var(--app-text-soft);
}
.instructor-cta {
  background: linear-gradient(135deg, rgba(234, 88, 12, 0.06), rgba(15, 118, 110, 0.06));
}
:global(.body--dark) .instructor-cta {
  background: linear-gradient(135deg, rgba(234, 88, 12, 0.16), rgba(15, 118, 110, 0.16));
}

.content-wrap {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
