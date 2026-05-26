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

      <!-- Sección destacada: Laboratorios virtuales -->
      <section class="labs-feature q-pa-lg q-pa-md-xl rounded-borders q-mb-xl">
        <div class="row q-col-gutter-xl items-center">
          <div class="col-12 col-md-6">
            <q-chip dense color="primary" text-color="white" icon="bolt" class="q-mb-md">
              Novedad
            </q-chip>
            <h2 class="text-h4 q-mt-none q-mb-sm labs-title">
              Laboratorios virtuales en la lección
            </h2>
            <p class="text-body1 q-mb-lg labs-subtitle">
              Conecta a una máquina Linux real desde el navegador y prueba lo que aprendes en segundos. Sin instalar nada, sin configurar entornos.
            </p>

            <ul class="labs-bullets q-mb-lg">
              <li>
                <q-icon name="terminal" size="22px" color="primary" />
                <span><strong>Entorno real</strong> — un Linux completo, no un simulador.</span>
              </li>
              <li>
                <q-icon name="dns" size="22px" color="primary" />
                <span><strong>Una VM por alumno</strong> — tu propio entorno aislado, asignado por tu instructor.</span>
              </li>
              <li>
                <q-icon name="open_in_full" size="22px" color="primary" />
                <span><strong>Consola embebida</strong> — abre el terminal sin salir de la lección.</span>
              </li>
              <li>
                <q-icon name="bolt" size="22px" color="primary" />
                <span><strong>Arranca al instante</strong> — un clic y estás dentro.</span>
              </li>
            </ul>

            <q-btn
              unelevated
              rounded
              color="primary"
              icon="play_circle"
              label="Ver un laboratorio"
              to="/cursos/python-principiantes"
              size="md"
            />
          </div>

          <div class="col-12 col-md-6">
            <!-- Mockup decorativo de terminal -->
            <div class="terminal-mock">
              <div class="terminal-mock__bar">
                <span class="terminal-mock__dot dot-red" />
                <span class="terminal-mock__dot dot-yellow" />
                <span class="terminal-mock__dot dot-green" />
                <span class="terminal-mock__title">student@curso-python</span>
              </div>
              <pre class="terminal-mock__body"><span class="t-prompt">$</span> <span class="t-cmd">python3 calculadora.py</span>
<span class="t-text">¿Cuántas asignaturas tienes?</span> <span class="t-user">3</span>
<span class="t-text">Nombre:</span> <span class="t-user">Matemáticas</span>
<span class="t-text">Nota:</span> <span class="t-user">8.5</span>
<span class="t-text">Nombre:</span> <span class="t-user">Historia</span>
<span class="t-text">Nota:</span> <span class="t-user">6.0</span>
<span class="t-text">Nombre:</span> <span class="t-user">Lengua</span>
<span class="t-text">Nota:</span> <span class="t-user">7.5</span>

<span class="t-ok">─────────────────────</span>
<span class="t-ok">Media: 7.33  ✓ Aprobada</span>
<span class="t-prompt">$</span> <span class="t-cursor">▊</span></pre>
            </div>
          </div>
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

/* ── Sección Laboratorios ────────────────────────────────────── */
.labs-feature {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.10), rgba(234, 88, 12, 0.08));
  border: 1px solid var(--app-border);
}
:global(.body--dark) .labs-feature {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.22), rgba(234, 88, 12, 0.14));
}
.labs-title {
  font-family: Monda, sans-serif;
  color: var(--app-text-strong);
  line-height: 1.2;
}
.labs-subtitle {
  color: var(--app-text-soft);
  font-size: 1.05rem;
}
.labs-bullets {
  list-style: none;
  padding: 0;
  margin: 0;
}
.labs-bullets li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
  color: var(--app-text);
}
.labs-bullets li strong {
  color: var(--app-text-strong);
}

/* Terminal mockup */
.terminal-mock {
  background: #1a1d21;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.25);
  font-family: 'Fira Code', 'JetBrains Mono', Menlo, Consolas, monospace;
}
.terminal-mock__bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #2a2f36;
  border-bottom: 1px solid #000;
}
.terminal-mock__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}
.dot-red    { background: #ff5f56; }
.dot-yellow { background: #ffbd2e; }
.dot-green  { background: #27c93f; }
.terminal-mock__title {
  margin-left: 12px;
  color: #c0c0c0;
  font-size: 12px;
  letter-spacing: 0.04em;
}
.terminal-mock__body {
  margin: 0;
  padding: 16px 18px;
  color: #d8d8d8;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.t-prompt { color: #27c93f; font-weight: bold; }
.t-cmd    { color: #61dafb; }
.t-text   { color: #c0c0c0; }
.t-user   { color: #f6c177; }
.t-ok     { color: #9ece6a; }
.t-cursor {
  color: #d8d8d8;
  animation: blink 1s steps(1) infinite;
}
@keyframes blink {
  50% { opacity: 0; }
}
</style>
