<script setup lang="ts">
import { computed } from 'vue'
import StarRating from './StarRating.vue'

interface Props {
  average: number
  total: number
  distribution: Record<1 | 2 | 3 | 4 | 5, number>
}

const props = defineProps<Props>()

// Calcula el porcentaje de cada nivel
const percentages = computed(() => {
  const total = props.total || 1
  return {
    5: props.distribution[5] / total,
    4: props.distribution[4] / total,
    3: props.distribution[3] / total,
    2: props.distribution[2] / total,
    1: props.distribution[1] / total
  }
})

const ratingLevels = [5, 4, 3, 2, 1] as const

</script>

<template>
  <div class="rating-distribution">

    <!-- Cabecera con promedio -->
    <div class="header">
      <div class="avg">{{ average.toFixed(1) }}</div>
      <div class="stars">
        <StarRating :model-value="average" readonly size="lg" />
      </div>
      <div class="total">{{ total }} valoraciones</div>
    </div>

    <!-- Barras 5 → 1 -->
    <div class="rows">
      <div
        v-for="n in ratingLevels"
        :key="n"
        class="row"
      >
        <div class="label">{{ n }} ★</div>

        <q-linear-progress
          :value="percentages[n]"
          color="amber"
          track-color="grey-3"
          size="12px"
          rounded
          class="bar"
        />

        <div class="count">{{ distribution[n] }}</div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.rating-distribution {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avg {
  font-size: 2.4rem;
  font-weight: bold;
}

.total {
  color: #666;
  font-size: 0.9rem;
}

.rows {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.label {
  width: 40px;
  text-align: right;
  font-weight: 600;
}

.bar {
  flex: 1;
}

.count {
  width: 40px;
  text-align: left;
  color: #555;
}
</style>
