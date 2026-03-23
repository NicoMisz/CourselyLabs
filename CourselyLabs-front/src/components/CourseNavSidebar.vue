<template>
  <q-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    bordered
    side="left"
    :width="300"
    :breakpoint="1024"
    class="bg-white"
  >
    <div class="q-pa-md">
      <router-link :to="`/cursos/${courseSlug}`" class="text-subtitle1 text-weight-medium text-primary" style="text-decoration: none">
        {{ courseTitle }}
      </router-link>

      <div v-if="progressText" class="q-mt-sm">
        <div class="text-caption text-grey-7 q-mb-xs">{{ progressText }}</div>
        <q-linear-progress
          :value="progressPercent / 100"
          color="primary"
          track-color="grey-3"
          rounded
          size="6px"
          class="progress-bar"
        />
      </div>
    </div>

    <q-separator />

    <q-list>
      <template v-for="section in sections" :key="section.id">
        <q-expansion-item
          :label="section.title"
          :caption="sectionCaption(section)"
          header-class="text-weight-medium text-body2"
          default-opened
          dense
        >
          <q-item
            v-for="lesson in section.lessons"
            :key="lesson.id"
            clickable
            :to="`/cursos/${courseSlug}/leccion/${lesson.id}`"
            :active="lesson.id === activeLessonId"
            active-class="bg-primary text-white"
            class="q-pl-lg"
            dense
          >
            <q-item-section avatar>
              <q-icon
                v-if="completedLessonIds.includes(lesson.id)"
                name="check_circle"
                size="18px"
                :color="lesson.id === activeLessonId ? 'white' : 'positive'"
              />
              <LessonTypeIcon
                v-else
                :type="lesson.type"
                size="18px"
                :color="lesson.id === activeLessonId ? 'white' : 'grey-7'"
              />
            </q-item-section>
            <q-item-section>
              <q-item-label class="text-body2">{{ lesson.title }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-expansion-item>
      </template>
    </q-list>
  </q-drawer>
</template>

<script setup lang="ts">
import type { Section } from '../types/lesson'
import LessonTypeIcon from './LessonTypeIcon.vue'

const props = withDefaults(defineProps<{
  modelValue: boolean
  sections: Section[]
  courseSlug: string
  courseTitle: string
  activeLessonId?: string
  completedLessonIds?: string[]
  progressPercent?: number
  progressText?: string
}>(), {
  completedLessonIds: () => [],
  progressPercent: 0,
  progressText: '',
})

defineEmits<{
  'update:modelValue': [value: boolean]
}>()

function sectionCaption(section: Section): string {
  const completed = section.lessons.filter(l => props.completedLessonIds.includes(l.id)).length
  const total = section.lessons.length
  return `${completed}/${total} leccion${total !== 1 ? 'es' : ''}`
}
</script>

<style scoped>
.progress-bar {
  transition: width 0.5s ease;
}
</style>
