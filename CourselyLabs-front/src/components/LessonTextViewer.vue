<template>
  <div class="lesson-text-viewer q-pa-lg" v-html="renderedHtml" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const props = defineProps<{
  content: string
}>()

marked.setOptions({
  highlight(code: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
})

const renderedHtml = computed(() => {
  return marked.parse(props.content) as string
})
</script>

<style scoped>
.lesson-text-viewer {
  max-width: 800px;
  line-height: 1.8;
}

.lesson-text-viewer :deep(pre) {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}

.lesson-text-viewer :deep(code) {
  font-size: 0.9em;
}

.lesson-text-viewer :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}
</style>
