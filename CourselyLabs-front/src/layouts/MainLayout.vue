<template>
  <q-layout view="hHh Lpr fff" :class="$q.dark.isActive ? 'bg-grey-10' : 'bg-grey-1'">
    <AppHeader
      @toggle-drawer="leftDrawerOpen = !leftDrawerOpen"
      @update:compact="headerCompact = $event"
    />

    <AppSidebar
      v-model="leftDrawerOpen"
      :mini="miniState"
      :header-hidden="headerCompact"
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
const headerCompact = ref(false)
</script>
