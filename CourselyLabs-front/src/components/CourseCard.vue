<template>
  <q-card class="course-card cursor-pointer" @click="$router.push(`/cursos/${course.slug}`)">
    <CourseThumbnail
      :thumbnail-url="course.thumbnailUrl"
      :title="course.title"
      :category-name="course.categoryName"
      class="course-card__thumb-wrapper"
      :rounded="false"
    />

    <q-card-section>
      <div class="text-h6 ellipsis card-title">{{ course.title }}</div>
      <div class="text-body2 ellipsis-2-lines q-mt-xs card-description" style="min-height: 40px">
        {{ course.shortDescription }}
      </div>
    </q-card-section>

    <q-card-section class="q-pt-none row items-center q-gutter-sm">
      <LevelBadge :level="course.level" />
      <PriceBadge :is-free="course.isFree" :price="course.price" />
      <q-space />
      <div v-if="course.averageRating" class="text-caption row items-center no-wrap rating-text">
        <q-icon name="star" color="warning" size="16px" class="q-mr-xs" />
        {{ course.averageRating.toFixed(1) }}
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import LevelBadge from './LevelBadge.vue'
import PriceBadge from './PriceBadge.vue'
import CourseThumbnail from './CourseThumbnail.vue'
import type { Course } from '@/types/course'

defineProps<{ course: Course }>()
</script>

<style scoped>
.course-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.course-card__thumb-wrapper {
  border-radius: 0;
}

.card-title {
  color: var(--app-text-strong);
}
.card-description {
  color: var(--app-text-soft);
}
.rating-text {
  color: var(--app-text-soft);
}
</style>
