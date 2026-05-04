<template>
  <div class="course-thumb" :style="rootStyle">
    <img
      v-if="src"
      :src="src"
      :alt="title || 'Curso'"
      class="course-thumb__img"
      @error="imgFailed = true"
    />
    <template v-else>
      <div class="course-thumb__fallback">
        <span class="course-thumb__emoji" :style="emojiStyle">{{ categoryEmoji }}</span>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface Props {
  thumbnailUrl?: string | null
  title?: string
  categorySlug?: string | null
  categoryName?: string | null
  /** Tamaño del emoji central. */
  iconSize?: string
  /** Forzar aspect-ratio (por defecto 16/9). Pasar `null` para que el componente se adapte al padre. */
  ratio?: number | null
  /** Border radius del wrapper. */
  rounded?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  thumbnailUrl: null,
  title: '',
  categorySlug: null,
  categoryName: null,
  iconSize: '64px',
  ratio: 16 / 9,
  rounded: true,
})

const imgFailed = ref(false)

const src = computed(() => (!imgFailed.value && props.thumbnailUrl ? props.thumbnailUrl : null))

const rootStyle = computed(() => ({
  aspectRatio: props.ratio === null ? undefined : String(props.ratio),
  borderRadius: props.rounded ? '8px' : '0',
}))

const emojiStyle = computed(() => ({
  fontSize: props.iconSize,
}))

// Emoji por categoría — coherente con la marca, ningún color extra.
const CATEGORY_EMOJI: Record<string, string> = {
  programacion: '💻',
  diseno: '🎨',
  negocios: '📈',
  marketing: '📢',
  idiomas: '🌍',
  musica: '🎵',
}

function slugifyCategoryName(name: string | null | undefined): string {
  if (!name) return ''
  return name
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

const categoryEmoji = computed(() => {
  const slug = props.categorySlug || slugifyCategoryName(props.categoryName)
  if (slug && CATEGORY_EMOJI[slug]) {
    return CATEGORY_EMOJI[slug]
  }
  return '📚'
})
</script>

<style scoped>
.course-thumb {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: var(--app-bg-soft);
}

.course-thumb__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.course-thumb__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--q-primary) 0%, var(--q-accent) 100%);
}

.course-thumb__emoji {
  line-height: 1;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));
  user-select: none;
}
</style>
