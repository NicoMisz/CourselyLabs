<template>
  <div class="price-wrap">
    <q-chip v-if="isFree" dense color="positive" text-color="white">Gratis</q-chip>
    <template v-else>
      <q-chip dense color="accent" text-color="white">{{ formattedPrice }}</q-chip>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  isFree: boolean
  price?: number | null
}>()

const formattedPrice = computed(() => {
  if (props.price != null && props.price > 0) {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(props.price)
  }
  return 'De pago'
})
</script>

<style scoped>
.price-wrap {
  display: flex;
  align-items: center;
}
</style>
