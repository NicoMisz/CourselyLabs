<template>
  <q-tab-panel name="instructores">
    <div v-if="instructors?.length" class="row q-col-gutter-md">
      <div
        v-for="instructor in instructors"
        :key="instructor.id"
        class="col-12 col-sm-6"
      >
        <q-card flat bordered class="q-pa-md row items-center q-gutter-md">
          <q-avatar color="primary" text-color="white" size="48px">
            <img v-if="instructor.avatarUrl" :src="instructor.avatarUrl" :alt="instructor.name" />
            <span v-else>{{ initials(instructor.name) }}</span>
          </q-avatar>
          <div>
            <div class="text-subtitle1 text-weight-medium">{{ instructor.name }}</div>
            <div class="text-body2 text-grey-7">{{ instructor.bio || 'Sin bio' }}</div>
          </div>
        </q-card>
      </div>
    </div>
    <q-banner v-else class="bg-grey-2 text-grey-8" rounded>
      No hay instructores asignados.
    </q-banner>
  </q-tab-panel>
</template>

<script setup lang="ts">
import type { InstructorSummary } from '@/types/course'

defineProps<{
  instructors?: InstructorSummary[]
}>()

function initials(name: string): string {
  return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
}
</script>
