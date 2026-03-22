<template>
  <div class="lesson-pdf-viewer">
    <embed :src="src" type="application/pdf" class="pdf-embed" />
    <div class="fallback q-mt-md text-center">
      <q-btn
        flat
        color="primary"
        icon="download"
        label="Descargar PDF"
        :href="src"
        target="_blank"
        type="a"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  src: string
}>()
</script>

<style scoped>
.lesson-pdf-viewer {
  width: 100%;
}

.pdf-embed {
  width: 100%;
  height: 80vh;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
}
</style>
