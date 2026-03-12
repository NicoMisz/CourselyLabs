<template>
  <div class="price-wrap">
    <q-chip v-if="isFree" dense color="positive" text-color="white">Gratis</q-chip>
    <div v-else class="price-paid">
      <span class="price-amount">{{ formattedPrice }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  free?: boolean;
  price?: number;
}>();

const isFree = computed(() => props.free || !props.price || props.price <= 0);

const formattedPrice = computed(() =>
  new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(props.price || 0),
);
</script>

<style scoped>
.price-wrap {
  display: flex;
  align-items: center;
}
.price-amount {
  font-weight: 700;
  font-size: 1.2rem;
}
</style>