<template>
  <q-tab-panel name="instructores" class="q-pa-none q-pt-md">
    <div v-if="instructors?.length" class="row q-col-gutter-md">
      <div
        v-for="instructor in sortedInstructors"
        :key="instructor.id"
        class="col-12 col-sm-6"
      >
        <q-card flat bordered class="instructor-card q-pa-md">
          <div class="row items-start q-gutter-md no-wrap">
            <q-avatar
              :color="instructor.isMain ? 'primary' : 'grey-5'"
              text-color="white"
              size="56px"
            >
              <img v-if="instructor.avatarUrl" :src="instructor.avatarUrl" :alt="instructor.name" />
              <span v-else>{{ initials(instructor.name) }}</span>
            </q-avatar>
            <div class="col">
              <div class="row items-center q-gutter-xs">
                <span class="text-subtitle1 text-weight-medium">{{ instructor.name || 'Instructor' }}</span>
                <q-badge
                  v-if="instructor.isMain"
                  color="primary"
                  text-color="white"
                  class="q-ml-xs"
                >
                  Principal
                </q-badge>
              </div>
              <div v-if="instructor.bio" class="text-body2 text-grey-7 q-mt-xs instructor-bio">
                {{ instructor.bio }}
              </div>
              <div v-else class="text-caption text-grey-5 q-mt-xs">
                Sin biografía
              </div>
            </div>
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
import { computed } from 'vue'
import type { InstructorSummary } from '@/types/course'

const props = defineProps<{
  instructors?: InstructorSummary[]
}>()

const sortedInstructors = computed(() => {
  if (!props.instructors) return []
  // El principal siempre primero (el backend ya ordena, esto es defensivo).
  return [...props.instructors].sort((a, b) =>
    Number(!!b.isMain) - Number(!!a.isMain)
  )
})

function initials(name: string): string {
  if (!name) return '?'
  return name.split(' ').map(w => w[0] || '').join('').toUpperCase().slice(0, 2)
}
</script>

<style scoped>
.instructor-card {
  height: 100%;
}
.instructor-bio {
  white-space: pre-line; /* Preserva saltos de línea de la bio */
  line-height: 1.5;
}
</style>
