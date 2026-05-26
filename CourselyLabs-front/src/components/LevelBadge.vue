<template>
  <q-chip
    dense
    :color="chipColor"
    text-color="white"
    icon="signal_cellular_alt"
    class="level-badge"
  >
    {{ label }}
  </q-chip>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ level?: string }>();

const normalized = computed(() => (props.level || '').trim().toLowerCase());

// Mapa idioma → etiqueta visible (acepta valores en inglés del backend o en castellano).
const LABELS: Record<string, string> = {
  beginner:      'Principiante',
  basic:         'Principiante',
  principiante:  'Principiante',
  intermediate:  'Intermedio',
  intermedio:    'Intermedio',
  advanced:      'Avanzado',
  avanzado:      'Avanzado',
  expert:        'Experto',
  experto:       'Experto',
};

const COLORS: Record<string, string> = {
  Principiante: 'positive',
  Intermedio:   'warning',
  Avanzado:     'deep-orange',
  Experto:      'negative',
};

const label = computed(() => LABELS[normalized.value] || 'Sin nivel');
const chipColor = computed(() => COLORS[label.value] || 'grey-7');
</script>

<style scoped>
.level-badge {
  font-weight: 500;
  letter-spacing: 0.01em;
}
</style>
