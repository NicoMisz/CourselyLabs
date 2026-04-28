<template>
  <div class="lesson-nav-bar row items-center justify-between q-pa-sm">
    <q-btn
      flat
      no-caps
      :disable="!prev"
      :to="prev ? `/cursos/${courseSlug}/leccion/${prev.id}` : undefined"
      icon="arrow_back"
      :label="prev ? prev.title : ''"
      class="col-5 text-left ellipsis-btn"
    />
    <q-btn
      flat
      no-caps
      :disable="!next"
      :to="next ? `/cursos/${courseSlug}/leccion/${next.id}` : undefined"
      icon-right="arrow_forward"
      :label="next ? next.title : ''"
      class="col-5 text-right ellipsis-btn"
    />
  </div>
</template>

<script setup lang="ts">
import type { Lesson } from '../types/lesson'

defineProps<{
  prev?: Lesson | null
  next?: Lesson | null
  courseSlug: string
}>()
</script>

<style scoped>
.lesson-nav-bar {
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  background: white;
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.ellipsis-btn :deep(.q-btn__content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
