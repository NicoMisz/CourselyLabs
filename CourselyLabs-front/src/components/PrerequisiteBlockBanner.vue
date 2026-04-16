<script setup lang="ts">
defineProps<{
    blockers: {
        title: string
        slug: string
        enrolled: boolean
        completed: boolean
        completedLessons: number
        totalLessons: number
    }[]
}>()
</script>

<template>
    <q-banner v-if="blockers.length" rounded class="bg-orange-1 text-orange-10 q-mb-md" inline-actions>
        Completa los cursos requeridos antes de inscribirte.
        <template #action>
        <q-btn flat color="orange-9" label="Ver prerequisitos" />
        </template>
    </q-banner>
</template>