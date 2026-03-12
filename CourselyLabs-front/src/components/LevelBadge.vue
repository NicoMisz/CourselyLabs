<template>
  <q-chip dense :color="chipColor" text-color="white" icon="school">
    {{ label }}
  </q-chip>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ level?: string }>();

const normalized = computed(() => (props.level || '').toUpperCase());

const chipColor = computed(() => {
  if (normalized.value.includes('PRINCIPIANTE')) return 'positive';
  if (normalized.value.includes('INTERMEDIO')) return 'warning';
  if (normalized.value.includes('AVANZADO')) return 'negative';
  return 'grey-7';
});

const label = computed(() => props.level || 'Sin nivel');
</script>