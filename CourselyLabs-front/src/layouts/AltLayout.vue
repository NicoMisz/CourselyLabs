<template>
  <q-layout view="hHh lpr fFf" class="bg-white">
    <AppHeader :show-menu-button="false" />

    <q-page-container>
      <router-view />
    </q-page-container>

    <AppFooter />
  </q-layout>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
import AppFooter from './AppFooter.vue'
</script>
