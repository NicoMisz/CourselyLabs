<script setup lang="ts">
import type { CoursePrerequisite } from '@/types/prerequisite'

defineProps<{
    prerequisites: CoursePrerequisite[]
    blockers: {
        prerequisiteCourseId: string
        title: string
        slug: string
        enrolled: boolean
        completed: boolean
        completedLessons: number
        totalLessons: number
        progressPercent: number
        requiredThreshold: number
    }[]
}>()
</script>

<template>
    <section v-if="prerequisites.length" class="q-mb-lg">
        <div class="text-h6 q-mb-md">Antes de empezar</div>

        <div class="row q-col-gutter-md">
            <div v-for="prereq in prerequisites" :key="prereq.id" class="col-12 col-sm-6">
            <q-card flat bordered class="q-pa-md">
                <div class="text-subtitle1 text-weight-medium">{{ prereq.prerequisiteTitle }}</div>
                <div class="text-body2 text-grey-7">Slug: {{ prereq.prerequisiteSlug }}</div>
            </q-card>
            </div>
        </div>
    </section>
</template>