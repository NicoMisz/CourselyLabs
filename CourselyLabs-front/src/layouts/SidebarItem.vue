<template>
  <q-item
    clickable
    :to="link"
    :active-class="exact ? undefined : 'text-primary'"
    :exact-active-class="exact ? 'text-primary' : undefined"
  >
    <q-item-section avatar>
      <q-icon :name="icon" />
      <q-tooltip v-if="mini" anchor="center right" self="center left" :offset="[10, 0]">
        {{ title }}
      </q-tooltip>
    </q-item-section>
    <q-item-section>
      <q-item-label>{{ title }}</q-item-label>
    </q-item-section>
  </q-item>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  icon: string
  link: string
  mini?: boolean
  /** Si true, solo se marca como activo cuando la ruta coincide exactamente (uso típico: link "/"). */
  exact?: boolean
}>(), {
  exact: false,
})
</script>
