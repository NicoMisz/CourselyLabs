<template>
  <div class="rich-editor">
    <div v-if="editor" class="editor-toolbar q-pa-xs q-mb-xs" style="border: 1px solid #ddd; border-radius: 4px 4px 0 0; background: #fafafa">
      <q-btn-group flat>
        <q-btn flat dense size="sm" icon="format_bold" :class="{ 'text-primary': editor.isActive('bold') }" @click="editor.chain().focus().toggleBold().run()" />
        <q-btn flat dense size="sm" icon="format_italic" :class="{ 'text-primary': editor.isActive('italic') }" @click="editor.chain().focus().toggleItalic().run()" />
        <q-btn flat dense size="sm" icon="code" :class="{ 'text-primary': editor.isActive('code') }" @click="editor.chain().focus().toggleCode().run()" />
      </q-btn-group>
      <q-btn-group flat class="q-ml-xs">
        <q-btn flat dense size="sm" icon="format_list_bulleted" :class="{ 'text-primary': editor.isActive('bulletList') }" @click="editor.chain().focus().toggleBulletList().run()" />
        <q-btn flat dense size="sm" icon="format_list_numbered" :class="{ 'text-primary': editor.isActive('orderedList') }" @click="editor.chain().focus().toggleOrderedList().run()" />
      </q-btn-group>
      <q-btn-group flat class="q-ml-xs">
        <q-btn flat dense size="sm" label="H2" :class="{ 'text-primary': editor.isActive('heading', { level: 2 }) }" @click="editor.chain().focus().toggleHeading({ level: 2 }).run()" />
        <q-btn flat dense size="sm" label="H3" :class="{ 'text-primary': editor.isActive('heading', { level: 3 }) }" @click="editor.chain().focus().toggleHeading({ level: 3 }).run()" />
      </q-btn-group>
      <q-btn-group flat class="q-ml-xs">
        <q-btn flat dense size="sm" icon="data_object" :class="{ 'text-primary': editor.isActive('codeBlock') }" @click="editor.chain().focus().toggleCodeBlock().run()" />
        <q-btn flat dense size="sm" icon="format_quote" :class="{ 'text-primary': editor.isActive('blockquote') }" @click="editor.chain().focus().toggleBlockquote().run()" />
      </q-btn-group>
    </div>
    <editor-content :editor="editor" class="editor-content" />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, watch } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight'
import { common, createLowlight } from 'lowlight'

const lowlight = createLowlight(common)

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
}>(), {
  modelValue: '',
  placeholder: 'Escribe aqui...',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

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
})

watch(() => props.modelValue, (val) => {
  if (editor.value && editor.value.getHTML() !== val) {
    editor.value.commands.setContent(val || '', false)
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<style>
.editor-content .tiptap {
  border: 1px solid #ddd;
  border-radius: 0 0 4px 4px;
  padding: 12px;
  min-height: 200px;
  outline: none;
}
.editor-content .tiptap:focus {
  border-color: #0f766e;
}
.editor-content .tiptap p.is-editor-empty:first-child::before {
  content: attr(data-placeholder);
  float: left;
  color: #adb5bd;
  pointer-events: none;
  height: 0;
}
.editor-content .tiptap pre {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
}
.editor-content .tiptap code {
  background: #f0f0f0;
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 0.9em;
}
.editor-content .tiptap pre code {
  background: none;
  padding: 0;
}
</style>
