<template>
  <q-layout view="hHh Lpr fff" :class="$q.dark.isActive ? 'bg-grey-10' : 'bg-grey-1'">
    <AppHeader @toggle-drawer="leftDrawerOpen = !leftDrawerOpen" />

    <AppSidebar
      v-model="leftDrawerOpen"
      :mini="miniState"
      @update:mini="miniState = $event"
    />

    <q-page-container>
      <router-view />
    </q-page-container>

    <AppFooter />
  </q-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import AppFooter from './AppFooter.vue'

const leftDrawerOpen = ref(true)
const miniState = ref(true)
</script>

<style scoped>
/*
  El AppFooter vive fuera del q-layout, así que q-page-container no le reserva espacio.
  Forzamos min-height para que en páginas con poco contenido el footer quede fuera de la
  viewport (solo aparece al hacer scroll), evitando que destaque sobre páginas casi vacías.
*/
:deep(.q-page-container) {
  min-height: calc(100vh - 64px);
}
</style>
