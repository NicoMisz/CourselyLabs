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
    </div>

    <q-separator />

    <q-list>
      <template v-for="section in sections" :key="section.id">
        <q-expansion-item
          :label="section.title"
          :caption="`${section.lessons.length} leccion${section.lessons.length !== 1 ? 'es' : ''}`"
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
              <LessonTypeIcon :type="lesson.type" size="18px" :color="lesson.id === activeLessonId ? 'white' : 'grey-7'" />
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

defineProps<{
  modelValue: boolean
  sections: Section[]
  courseSlug: string
  courseTitle: string
  activeLessonId?: string
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>
