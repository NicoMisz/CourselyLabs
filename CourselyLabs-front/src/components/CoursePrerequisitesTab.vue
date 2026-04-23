<script setup lang="ts">
    import type { CoursePrerequisiteStatus, RelatedCourseItem } from '@/types/prerequisite'

    defineProps<{
        statuses: CoursePrerequisiteStatus[]
        requiredBy?: RelatedCourseItem[]
        loading?: boolean
    }>()
</script>

<template>
    <section>
        <q-skeleton v-if="loading" type="rect" height="90px" class="q-mb-sm" />
        
        <template v-else>
            <q-banner v-if="!statuses.length && !requiredBy?.length" class="bg-grey-2 text-grey-8" rounded>
                Este curso no tiene prerequisitos o no hay estado disponible.
            </q-banner>

            <template v-if="statuses.length">
                <div class="text-subtitle1 q-mb-sm">Necesitas completar antes</div>
                <div class="row q-col-gutter-md q-mb-md">
                    <div v-for="s in statuses" :key="s.prerequisiteCourseId" class="col-12 col-sm-6">
                        <q-card flat bordered class="q-pa-md">
                            <router-link :to="`/cursos/${s.slug}`" class="text-primary text-weight-medium">
                                {{ s.title }}
                            </router-link>
                            <div class="text-caption text-grey-7 q-mt-xs">
                                Progreso: {{ s.progressPercent }}% · Requerido: {{ s.requiredThreshold }}%
                            </div>
                            <div class="text-caption" :class="s.completed ? 'text-positive' : 'text-orange-9'">
                                {{ s.completed ? 'Completado' : 'Pendiente' }}
                            </div>
                        </q-card>
                    </div>
                </div>
            </template>

                <!-- Bloque "Este curso desbloquea" — visible cuando el backend exponga requiredBy -->
            <template v-if="requiredBy?.length">
                <div class="text-subtitle1 q-mb-sm">Este curso desbloquea</div>
                <div class="row q-col-gutter-md">
                    <div v-for="r in requiredBy" :key="r.courseId" class="col-12 col-sm-6">
                    <q-card flat bordered class="q-pa-md">
                        <router-link :to="`/cursos/${r.slug}`" class="text-primary text-weight-medium">
                        {{ r.title }}
                        </router-link>
                        <div class="text-caption text-grey-7 q-mt-xs">
                        Umbral: {{ r.completionThreshold ?? 80 }}%
                        </div>
                    </q-card>
                    </div>
                </div>
            </template>
        </template>
    </section>
</template>