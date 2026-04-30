<script setup lang="ts">
import { computed } from 'vue'
import StarRating from './StarRating.vue'
import { date } from 'quasar'

interface Props {
  id: string
  userName: string
  userAvatar?: string | null
  rating: number
  comment: string
  createdAt: string
  isOwn?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isOwn: false
})

const emit = defineEmits<{
  edit: [id: string]
  delete: [id: string]
}>()

const initials = computed(() =>
  props.userName
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
)

const relativeDate = computed(() =>
  date.formatDate(props.createdAt, 'DD MMM YYYY')
)
</script>

<template>
  <div class="review-item q-pa-md q-mb-md app-surface-soft rounded-borders">

    <!-- Header -->
    <div class="row items-center q-gutter-sm">

      <!-- Avatar -->
      <q-avatar size="42px">
        <img v-if="userAvatar" :src="userAvatar" />
        <span v-else>{{ initials }}</span>
      </q-avatar>

      <div class="col">
        <div class="row items-center q-gutter-xs">
          <span class="text-weight-medium">{{ userName }}</span>

          <q-badge v-if="isOwn" color="primary" outline>
            Tu valoración
          </q-badge>
        </div>

        <div class="text-grey-7 text-caption">
          {{ relativeDate }}
        </div>
      </div>

      <!-- Acciones si es propia -->
      <div v-if="isOwn" class="row q-gutter-xs">
        <q-btn flat dense icon="edit" @click="emit('edit', id)" />
        <q-btn flat dense icon="delete" color="negative" @click="emit('delete', id)" />
      </div>
    </div>

    <!-- Rating -->
    <div class="q-mt-sm">
      <StarRating :model-value="rating" readonly size="sm" />
    </div>

    <!-- Comentario -->
    <div class="q-mt-sm text-body2">
      {{ comment }}
    </div>

  </div>
</template>
