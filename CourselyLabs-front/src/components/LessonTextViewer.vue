<template>
  <div class="rich-content q-pa-lg" v-html="content" />
</template>

<script setup lang="ts">
defineProps<{ content: string }>()
</script>

<style>
.rich-content {
  max-width: 800px;
  line-height: 1.75;
  font-size: 16px;
  color: #1f2937;
}

.rich-content h1,
.rich-content h2 {
  font-family: 'Monda', sans-serif;
  font-weight: 700;
  color: #0f172a;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.rich-content h2 {
  font-size: 1.75rem;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.3em;
}

.rich-content h3 {
  font-size: 1.35rem;
  font-weight: 600;
  margin-top: 1.25em;
  margin-bottom: 0.4em;
  color: #0f172a;
}

.rich-content p {
  margin: 0 0 1em;
}

.rich-content ul,
.rich-content ol {
  padding-left: 1.75em;
  margin: 0 0 1em;
}

.rich-content li {
  margin-bottom: 0.35em;
}

.rich-content strong {
  font-weight: 700;
  color: #0f172a;
}

.rich-content em {
  font-style: italic;
}

.rich-content blockquote {
  border-left: 4px solid #0f766e;
  padding: 10px 16px;
  margin: 1em 0;
  color: #4b5563;
  font-style: italic;
  background: #f0fdfa;
  border-radius: 0 8px 8px 0;
}

.rich-content code {
  background: #f1f5f9;
  color: #0f766e;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'Fira Code', monospace;
}

.rich-content pre {
  background: #1e293b;
  color: #e2e8f0;
  padding: 14px 18px;
  border-radius: 8px;
  overflow-x: auto;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  margin: 1em 0;
}

.rich-content pre code {
  background: none;
  color: inherit;
  padding: 0;
}

.rich-content img {
  max-width: 100%;
  border-radius: 8px;
  margin: 1em 0;
}

.rich-content a {
  color: #0f766e;
  text-decoration: underline;
}
</style>
