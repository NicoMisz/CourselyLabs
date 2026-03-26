<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue: number
  readonly?: boolean
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false,
  size: 'md'
})

const emit = defineEmits(['update:modelValue'])

const labels = ['Malo', 'Regular', 'Bueno', 'Muy bueno', 'Excelente']

const starSize = computed(() => {
  return {
    sm: '16px',
    md: '24px',
    lg: '32px'
  }[props.size]
})

function setRating(value: number) {
  if (!props.readonly) {
    emit('update:modelValue', value)
  }
}

function getLabel(value: number) {
  return labels[Math.ceil(value) - 1] || ''
}
</script>

<template>
  <div class="star-rating" :title="getLabel(modelValue)">
    <div
      v-for="i in 5"
      :key="i"
      class="star-wrapper"
      @click="setRating(i)"
    >
      <!-- Fondo estrella vacía -->
      <svg
        class="star empty"
        :width="starSize"
        :height="starSize"
        viewBox="0 0 24 24"
      >
        <path
          d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
        />
      </svg>

      <!-- Estrella rellena con clip para parciales -->
      <svg
        class="star filled"
        :width="starSize"
        :height="starSize"
        viewBox="0 0 24 24"
        :style="{
          clipPath: `inset(0 ${(1 - Math.min(Math.max(modelValue - i + 1, 0), 1)) * 100}% 0 0)`
        }"
      >
        <path
          d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
        />
      </svg>
    </div>
  </div>
</template>

<style scoped>
.star-rating {
  display: flex;
  gap: 4px;
  cursor: pointer;
}

.star-wrapper {
  position: relative;
}

.star {
  fill: #ddd;
}

.star.filled {
  fill: #ffc107;
  position: absolute;
  top: 0;
  left: 0;
}

.star-rating[readonly] {
  cursor: default;
}
</style>
