<template>
  <q-layout view="hHh Lpr fFf" class="bg-white">
    <AppHeader
      @toggle-drawer="leftDrawerOpen = !leftDrawerOpen"
      @update:hidden="headerHidden = $event"
    />

    <AppSidebar
      v-model="leftDrawerOpen"
      :mini="miniState"
      :header-hidden="headerHidden"
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
const headerHidden = ref(false)
</script>
