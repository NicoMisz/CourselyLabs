<template>
  <div class="rich-content q-pa-lg" style="max-width: 800px;" v-html="content" />
</template>

<script setup lang="ts">
defineProps<{ content: string }>()
</script>

<!-- Estilos globales de .rich-content viven en src/css/app.scss -->
