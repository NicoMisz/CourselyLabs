<script setup lang="ts">
import { computed, ref } from 'vue'
import ReviewItem from './ReviewItem.vue'

import type { Review } from '../types/review'

interface Props {
  reviews: Review[]
  loadingMore?: boolean
  hasMore?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loadingMore: false,
  hasMore: false
})

const emit = defineEmits<{
  loadMore: []
  edit: [id: string]
  delete: [id: string]
}>()

const sortMode = ref<'recent' | 'best' | 'worst'>('recent')

const sortedReviews = computed(() => {
  const list = [...props.reviews].map(r => ({
    ...r,
    userName: r.userFullName ?? 'Usuario',
    createdAt: r.createdAt ?? ''
  }))

  // Priorizar review propia
  list.sort((a, b) => (b.isOwn ? 1 : 0) - (a.isOwn ? 1 : 0))

  // Orden seleccionado
  if (sortMode.value === 'recent') {
    return list.sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''))
  }
  if (sortMode.value === 'best') {
    return list.sort((a, b) => b.rating - a.rating)
  }
  if (sortMode.value === 'worst') {
    return list.sort((a, b) => a.rating - b.rating)
  }

  return list
})

const sortOptions =[
          { label: 'Más recientes', value: 'recent' },
          { label: 'Mejor valoración', value: 'best' },
          { label: 'Peor valoración', value: 'worst' }
        ] as const
</script>

<template>
  <div class="review-list">

    <!-- Selector de orden -->
    <div class="row justify-end q-mb-md">
      <q-select
        v-model="sortMode"
        dense
        outlined
        emit-value
        map-options
        style="width: 180px"
        :options="sortOptions"
      />
    </div>

    <!-- Lista -->
    <ReviewItem
      v-for="r in sortedReviews"
      :key="r.id"
      v-bind="r"
      @edit="emit('edit', r.id)"
      @delete="emit('delete', r.id)"
    />

    <!-- Cargar más -->
    <div class="row justify-center q-mt-md" v-if="hasMore">
      <q-btn
        color="primary"
        outline
        label="Cargar más"
        :loading="loadingMore"
        @click="emit('loadMore')"
      />
    </div>

  </div>
</template>
