<template>
    <q-page class="q-pa-md q-pa-lg-md">
        <div class="page-wrap">
        <div class="row items-center justify-between q-mb-lg">
            <div>
            <h1 class="text-h4 q-my-none">Mis cursos</h1>
            <p class="text-body1 text-grey-7 q-mt-sm q-mb-none">
                Retoma tus cursos y continua donde lo dejaste.
            </p>
            </div>
        </div>

        <div class="row items-center justify-between q-col-gutter-md q-mb-lg">
            <div class="col-12 col-md-auto">
            <q-tabs v-model="filter" dense align="left" active-color="primary" indicator-color="primary">
                <q-tab name="todos" label="Todos" />
                <q-tab name="en-curso" label="En curso" />
                <q-tab name="completados" label="Completados" />
            </q-tabs>
            </div>

            <div class="col-12 col-md-3">
            <q-select
                v-model="sortBy"
                :options="sortOptions"
                outlined
                dense
                emit-value
                map-options
                label="Ordenar por"
            />
            </div>
        </div>

        <q-banner v-if="errorMessage" rounded class="bg-red-1 text-negative q-mb-md" inline-actions>
            {{ errorMessage }}
            <template #action>
            <q-btn flat color="negative" label="Reintentar" @click="fetchMyCourses" />
            </template>
        </q-banner>

        <div v-if="loading" class="row q-col-gutter-lg">
            <div v-for="n in 3" :key="n" class="col-12 col-md-4">
            <q-skeleton type="rect" height="260px" />
            </div>
        </div>

        <EmptyState
            v-else-if="!courses.length"
            title="Aun no te has inscrito en ningun curso"
            description="Explora el catalogo y empieza por el curso que mas encaje contigo."
            cta-label="Explorar cursos"
            @action="goToCatalog"
        />

        <div v-else class="row q-col-gutter-lg">
            <div v-for="course in visibleCourses" :key="course.enrollmentId" class="col-12 col-md-4">
                <CourseCardEnrolled :course="course" />
            </div>
        </div>
        </div>
    </q-page>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import EmptyState from '../components/EmptyState.vue';
import CourseCardEnrolled from '../components/CourseCardEnrolled.vue';
import { getMyCourses } from '../api/enrollment';
import type { EnrolledCourse } from '../types/enrollment';

const router = useRouter();
const loading = ref(true);
const errorMessage = ref('');
const courses = ref<EnrolledCourse[]>([]);

const filter = ref<'todos' | 'en-curso' | 'completados'>('todos');
const sortBy = ref<'lastAccessedAt' | 'title' | 'progressPercent'>('lastAccessedAt');

const sortOptions = [
  { label: 'Ultimo acceso', value: 'lastAccessedAt' },
  { label: 'Nombre', value: 'title' },
  { label: 'Progreso', value: 'progressPercent' },
];

const visibleCourses = computed(() => {
  const filtered = courses.value.filter((course) => {
    if (filter.value === 'completados') return course.progressPercent >= 100;
    if (filter.value === 'en-curso') return course.progressPercent > 0 && course.progressPercent < 100;
    return true;
  });

  return [...filtered].sort((a, b) => {
    if (sortBy.value === 'title') return a.title.localeCompare(b.title, 'es');
    if (sortBy.value === 'progressPercent') return b.progressPercent - a.progressPercent;

    const aTime = a.lastAccessedAt ? new Date(a.lastAccessedAt).getTime() : 0;
    const bTime = b.lastAccessedAt ? new Date(b.lastAccessedAt).getTime() : 0;
    return bTime - aTime;
  });
});

async function fetchMyCourses() {
    loading.value = true;
    errorMessage.value = '';

    try {
        courses.value = await getMyCourses();
    } catch {
        errorMessage.value = 'No se pudieron cargar tus cursos. Intenta nuevamente.';
    } finally {
        loading.value = false;
    }
}

function goToCatalog() {
    router.push('/cursos');
}

onMounted(fetchMyCourses);
</script>

<style scoped>
.page-wrap {
    max-width: 1200px;
    margin: 0 auto;
}
</style>