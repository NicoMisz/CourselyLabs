<script setup lang="ts">
    import type { CoursePrerequisiteStatus } from '@/types/prerequisite'

    defineProps<{
        statuses: CoursePrerequisiteStatus[]
        loading?: boolean
    }>()
</script>

<template>
    <section>
        <q-skeleton v-if="loading" type="rect" height="90px" class="q-mb-sm" />

        <q-banner v-else-if="!statuses.length" class="bg-grey-2 text-grey-8" rounded>
            Este curso no tiene prerequisitos o no hay estado disponible.
        </q-banner>

        <div v-else class="row q-col-gutter-md">
            <div v-for="s in statuses" :key="s.prerequisiteCourseId" class="col-12 col-sm-6">
                <q-card flat bordered class="q-pa-md">
                    <div class="text-subtitle1 text-weight-medium">{{ s.title }}</div>
                    <div class="text-body2 text-grey-7">Slug: {{ s.slug }}</div>
                    <div class="text-caption text-grey-7 q-mt-xs">
                        Progreso: {{ s.progressPercent }}% · Requerido: {{ s.requiredThreshold }}%
                    </div>
                    <div class="text-caption" :class="s.completed ? 'text-positive' : 'text-orange-9'">
                        {{ s.completed ? 'Completado' : 'Pendiente' }}
                    </div>
                </q-card>
            </div>
        </div>
    </section>
</template>