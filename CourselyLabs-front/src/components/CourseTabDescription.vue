<template>
  <q-tab-panel name="descripción">
    <p v-if="description">{{ description }}</p>
    <p v-else class="text-grey-6">Sin descripción completa por ahora.</p>
  </q-tab-panel>
</template>

<script setup lang="ts">
defineProps<{
  description?: string
}>()
</script>
