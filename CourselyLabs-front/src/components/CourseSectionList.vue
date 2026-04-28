<template>
  <div class="course-section-list">
    <q-list separator>
      <q-expansion-item
        v-for="section in sections"
        :key="section.id"
        :label="section.title"
        :caption="sectionCaption(section)"
        header-class="text-weight-medium"
        default-opened
        dense
      >
        <q-item
          v-for="lesson in section.lessons"
          :key="lesson.id"
          clickable
          :to="lessonRoute(lesson)"
          :active="lesson.id === activeLessonId"
          active-class="bg-primary text-white"
          class="q-pl-lg"
          dense
        >
          <q-item-section avatar>
            <LessonTypeIcon :type="(lesson.type || 'text') as any" :color="lesson.id === activeLessonId ? 'white' : 'grey-7'" />
          </q-item-section>

          <q-item-section>
            <q-item-label :class="lesson.id === activeLessonId ? '' : ''">
              {{ lesson.title }}
            </q-item-label>
            <q-item-label caption :class="lesson.id === activeLessonId ? 'text-white-7' : ''">
              <span v-if="lesson.duration">{{ formatDuration(lesson.duration) }}</span>
              <q-badge v-if="lesson.isFree && !enrolled" color="accent" label="Preview" class="q-ml-sm" />
            </q-item-label>
          </q-item-section>

          <q-item-section side v-if="!enrolled && !lesson.isFree">
            <q-icon name="lock" size="16px" color="grey-5" />
          </q-item-section>
        </q-item>
      </q-expansion-item>
    </q-list>
  </div>
</template>

<script setup lang="ts">
import type { Section, Lesson } from '../types/lesson'
import LessonTypeIcon from './LessonTypeIcon.vue'

const props = defineProps<{
  sections: Section[]
  courseSlug: string
  activeLessonId?: string
  enrolled?: boolean
}>()

function sectionCaption(section: Section): string {
  const count = section.lessons.length
  return `${count} lección${count !== 1 ? 'es' : ''}`
}

function lessonRoute(lesson: Lesson): string {
  if (!props.enrolled && !lesson.isFree) return ''
  return `/cursos/${props.courseSlug}/leccion/${lesson.id}`
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? `${m}:${String(s).padStart(2, '0')}` : `0:${String(s).padStart(2, '0')}`
}
</script>
