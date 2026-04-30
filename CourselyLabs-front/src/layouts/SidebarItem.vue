<template>
  <!--
    Para el item de "Inicio" (link "/"), usamos `exact` true para que solo se marque
    activo cuando la ruta es exactamente "/" — sin esto, el prefix match del router
    dejaría "Inicio" siempre marcado.
  -->
  <q-item
    v-if="exact"
    clickable
    :to="link"
    exact
    active-class="text-primary"
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

  <q-item
    v-else
    clickable
    :to="link"
    active-class="text-primary"
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
  exact?: boolean
}>(), {
  exact: false,
})
</script>
