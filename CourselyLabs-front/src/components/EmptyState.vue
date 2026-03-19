<template>
  <div class="empty-state column items-center q-pa-xl text-center">
    <q-icon :name="icon" size="64px" color="grey-5" class="q-mb-md" />
    <div class="text-h6 text-grey-8 q-mb-xs">{{ title }}</div>
    <div v-if="description" class="text-body2 text-grey-6 q-mb-lg" style="max-width: 360px">
      {{ description }}
    </div>
    <q-btn
      v-if="ctaLabel"
      unelevated
      color="accent"
      :label="ctaLabel"
      no-caps
      @click="$emit('action')"
    />
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    description?: string
    ctaLabel?: string
    icon?: string
  }>(),
  { icon: 'school' },
)

defineEmits<{ action: [] }>()
</script>

<style scoped>
.empty-state {
  min-height: 280px;
}
</style>
