<template>
  <div class="rich-editor" :class="{ 'rich-editor--focused': focused }">
    <div v-if="editor" class="editor-toolbar">
      <q-btn-group flat unelevated>
        <q-btn
          flat dense size="sm" icon="format_bold"
          :class="{ 'active': editor.isActive('bold') }"
          @click="editor.chain().focus().toggleBold().run()"
        >
          <q-tooltip>Negrita</q-tooltip>
        </q-btn>
        <q-btn
          flat dense size="sm" icon="format_italic"
          :class="{ 'active': editor.isActive('italic') }"
          @click="editor.chain().focus().toggleItalic().run()"
        >
          <q-tooltip>Cursiva</q-tooltip>
        </q-btn>
        <q-btn
          flat dense size="sm" icon="strikethrough_s"
          :class="{ 'active': editor.isActive('strike') }"
          @click="editor.chain().focus().toggleStrike().run()"
        >
          <q-tooltip>Tachado</q-tooltip>
        </q-btn>
        <q-btn
          flat dense size="sm" icon="code"
          :class="{ 'active': editor.isActive('code') }"
          @click="editor.chain().focus().toggleCode().run()"
        >
          <q-tooltip>Codigo inline</q-tooltip>
        </q-btn>
      </q-btn-group>

      <div class="toolbar-divider" />

      <q-btn-group flat unelevated>
        <q-btn
          flat dense size="sm"
          :class="{ 'active': editor.isActive('heading', { level: 2 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
        >
          <span class="text-weight-bold">H2</span>
          <q-tooltip>Título</q-tooltip>
        </q-btn>
        <q-btn
          flat dense size="sm"
          :class="{ 'active': editor.isActive('heading', { level: 3 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
        >
          <span class="text-weight-bold">H3</span>
          <q-tooltip>Subtitulo</q-tooltip>
        </q-btn>
      </q-btn-group>

      <div class="toolbar-divider" />

      <q-btn-group flat unelevated>
        <q-btn
          flat dense size="sm" icon="format_list_bulleted"
          :class="{ 'active': editor.isActive('bulletList') }"
          @click="editor.chain().focus().toggleBulletList().run()"
        >
          <q-tooltip>Lista</q-tooltip>
        </q-btn>
        <q-btn
          flat dense size="sm" icon="format_list_numbered"
          :class="{ 'active': editor.isActive('orderedList') }"
          @click="editor.chain().focus().toggleOrderedList().run()"
        >
          <q-tooltip>Lista numerada</q-tooltip>
        </q-btn>
        <q-btn
          flat dense size="sm" icon="format_quote"
          :class="{ 'active': editor.isActive('blockquote') }"
          @click="editor.chain().focus().toggleBlockquote().run()"
        >
          <q-tooltip>Cita</q-tooltip>
        </q-btn>
        <q-btn
          flat dense size="sm" icon="data_object"
          :class="{ 'active': editor.isActive('codeBlock') }"
          @click="editor.chain().focus().toggleCodeBlock().run()"
        >
          <q-tooltip>Bloque de codigo</q-tooltip>
        </q-btn>
      </q-btn-group>

      <div class="toolbar-divider" />

      <q-btn-group flat unelevated>
        <q-btn
          flat dense size="sm" icon="undo"
          :disable="!editor.can().undo()"
          @click="editor.chain().focus().undo().run()"
        >
          <q-tooltip>Deshacer</q-tooltip>
        </q-btn>
        <q-btn
          flat dense size="sm" icon="redo"
          :disable="!editor.can().redo()"
          @click="editor.chain().focus().redo().run()"
        >
          <q-tooltip>Rehacer</q-tooltip>
        </q-btn>
      </q-btn-group>
    </div>

    <editor-content :editor="editor" class="editor-content" />
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, watch } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight'
import { common, createLowlight } from 'lowlight'

const lowlight = createLowlight(common)

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  minHeight?: string
}>(), {
  modelValue: '',
  placeholder: 'Escribe aquí... Puedes usar negrita, cursiva, listas y mas.',
  minHeight: '260px',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const focused = ref(false)

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({ codeBlock: false }),
    Placeholder.configure({ placeholder: props.placeholder }),
    CodeBlockLowlight.configure({ lowlight }),
  ],
  onUpdate: () => {
    emit('update:modelValue', editor.value?.getHTML() || '')
  },
  onFocus: () => { focused.value = true },
  onBlur: () => { focused.value = false },
})

watch(() => props.modelValue, (val) => {
  if (editor.value && editor.value.getHTML() !== val) {
    editor.value.commands.setContent(val || '', { emitUpdate: false })
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<style scoped>
.rich-editor {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  overflow: hidden;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.rich-editor--focused {
  border-color: #0f766e;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  padding: 8px 10px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: #d1d5db;
  margin: 0 4px;
}

.editor-toolbar .q-btn {
  border-radius: 6px;
  min-width: 32px;
  padding: 4px 8px;
}

.editor-toolbar .q-btn.active {
  background: rgba(15, 118, 110, 0.12);
  color: #0f766e;
}
</style>

<style>
/* Global TipTap content styles (not scoped so they apply to children) */
.rich-editor .editor-content {
  min-height: v-bind(minHeight);
  max-height: 500px;
  overflow-y: auto;
}

.rich-editor .tiptap {
  padding: 16px 18px;
  outline: none;
  min-height: v-bind(minHeight);
  font-size: 15px;
  line-height: 1.6;
  color: #1f2937;
}

.rich-editor .tiptap p.is-editor-empty:first-child::before {
  content: attr(data-placeholder);
  float: left;
  color: #9ca3af;
  pointer-events: none;
  height: 0;
  font-style: italic;
}

.rich-editor .tiptap > * + * {
  margin-top: 0.75em;
}

.rich-editor .tiptap h2 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-top: 1.25em;
  color: #0f172a;
  font-family: 'Monda', sans-serif;
}

.rich-editor .tiptap h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-top: 1em;
  color: #0f172a;
}

.rich-editor .tiptap ul,
.rich-editor .tiptap ol {
  padding-left: 1.5em;
}

.rich-editor .tiptap li > p {
  margin: 0;
}

.rich-editor .tiptap blockquote {
  border-left: 4px solid #0f766e;
  padding-left: 1em;
  color: #4b5563;
  font-style: italic;
  background: #f0fdfa;
  padding: 8px 16px;
  border-radius: 0 6px 6px 0;
}

.rich-editor .tiptap code {
  background: #f1f5f9;
  color: #0f766e;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'Fira Code', monospace;
}

.rich-editor .tiptap pre {
  background: #1e293b;
  color: #e2e8f0;
  padding: 14px 18px;
  border-radius: 8px;
  overflow-x: auto;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
}

.rich-editor .tiptap pre code {
  background: none;
  color: inherit;
  padding: 0;
  border-radius: 0;
}

.rich-editor .tiptap strong {
  font-weight: 700;
  color: #0f172a;
}
</style>
