<template>
  <q-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    bordered
    side="left"
    :width="300"
    :breakpoint="1024"
    :class="$q.dark.isActive ? 'bg-grey-9' : 'bg-white'"
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

      <q-banner v-if="!enrolled" rounded class="bg-amber-1 text-amber-9 q-mt-sm" dense>
        <template #avatar>
          <q-icon name="lock" color="amber-9" size="18px" />
        </template>
        <div class="text-caption">
          Estás viendo lecciones gratuitas. Inscríbete para acceder al curso completo.
        </div>
        <template #action>
          <q-btn
            flat dense no-caps color="amber-9"
            :to="`/cursos/${courseSlug}`"
            label="Inscribirme"
          />
        </template>
      </q-banner>
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
            :clickable="canAccess(lesson)"
            :to="canAccess(lesson) ? `/cursos/${courseSlug}/leccion/${lesson.id}` : undefined"
            :active="lesson.id === activeLessonId"
            active-class="bg-primary text-white"
            class="q-pl-lg"
            dense
            :class="{ 'lesson-locked': !canAccess(lesson) }"
            @click="!canAccess(lesson) && handleLockedClick($event)"
          >
            <q-item-section avatar>
              <q-icon
                v-if="!canAccess(lesson)"
                name="lock"
                size="18px"
                color="grey-5"
              />
              <q-icon
                v-else-if="completedLessonIds.includes(lesson.id)"
                name="check_circle"
                size="18px"
                :color="lesson.id === activeLessonId ? 'white' : 'positive'"
              />
              <LessonTypeIcon
                v-else
                :type="(lesson.type || 'text') as any"
                size="18px"
                :color="lesson.id === activeLessonId ? 'white' : 'grey-7'"
              />
            </q-item-section>
            <q-item-section>
              <q-item-label class="text-body2">{{ lesson.title }}</q-item-label>
              <q-item-label v-if="lesson.isFree && !enrolled" caption>
                <q-badge color="accent" label="Preview" dense />
              </q-item-label>
            </q-item-section>
          </q-item>
        </q-expansion-item>
      </template>
    </q-list>

    <q-separator class="q-my-sm" />

    <q-list dense>
      <q-item clickable @click="toggleDark">
        <q-item-section avatar>
          <q-icon :name="$q.dark.isActive ? 'light_mode' : 'dark_mode'" />
        </q-item-section>
        <q-item-section>{{ $q.dark.isActive ? 'Modo claro' : 'Modo oscuro' }}</q-item-section>
      </q-item>
    </q-list>
  </q-drawer>
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar'
import type { Section, Lesson } from '../types/lesson'
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
  /** Si está inscrito, puede acceder a todas las lecciones. Si no, solo a las isFree. */
  enrolled?: boolean
}>(), {
  completedLessonIds: () => [],
  progressPercent: 0,
  progressText: '',
  enrolled: false,
})

defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const $q = useQuasar()

function toggleDark() {
  const next = !$q.dark.isActive
  $q.dark.set(next)
  localStorage.setItem('coursely-dark', next ? '1' : '0')
}

function canAccess(lesson: Lesson): boolean {
  return props.enrolled || !!lesson.isFree
}

function handleLockedClick(e: MouseEvent) {
  e.preventDefault()
  e.stopPropagation()
  $q.notify({
    type: 'info',
    message: 'Esta lección requiere inscripción al curso.',
    position: 'bottom-right',
  })
}

function sectionCaption(section: Section): string {
  const completed = section.lessons.filter(l => props.completedLessonIds.includes(l.id)).length
  const total = section.lessons.length
  return `${completed}/${total} lección${total !== 1 ? 'es' : ''}`
}
</script>

<style scoped>
.progress-bar {
  transition: width 0.5s ease;
}
.lesson-locked {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
