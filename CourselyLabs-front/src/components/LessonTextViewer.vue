<template>
  <div class="rich-content q-pa-lg" style="max-width: 800px;" v-html="rendered" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps<{ content: string }>()

// Heurística: si el contenido empieza con `<` (alguna etiqueta HTML), asumimos
// que ya es HTML — viene del editor TipTap que produce HTML estructurado.
// Si no, lo tratamos como Markdown (caso de los seeds antiguos que guardan
// `# Título`, `**bold**`, listas, tablas, etc. en texto plano).
//
// `marked` también respeta el HTML embebido, así que aunque la heurística
// fallara y nos llegara HTML, no lo rompería; pero evitamos parsear de más.
const rendered = computed(() => {
  const raw = props.content || ''
  if (raw.trimStart().startsWith('<')) return raw
  return marked.parse(raw, { gfm: true, breaks: false }) as string
})
</script>

<!-- Estilos globales de .rich-content viven en src/css/app.scss -->
