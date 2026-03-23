<template>
  <q-icon :name="iconName" :size="size" :color="color" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { LessonType } from '../types/lesson'

const props = withDefaults(defineProps<{
  type: LessonType
  size?: string
  color?: string
}>(), {
  size: '20px',
  color: 'grey-7',
})

const iconName = computed(() => {
  switch (props.type) {
    case 'video': return 'play_circle'
    case 'text': return 'article'
    case 'pdf': return 'picture_as_pdf'
    case 'audio': return 'headphones'
    default: return 'description'
  }
})
</script>
