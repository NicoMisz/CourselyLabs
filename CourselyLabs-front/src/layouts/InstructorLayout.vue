<template>
  <q-layout view="lHh Lpr lFf">
    <q-header bordered class="bg-white text-dark">
      <q-toolbar>
        <q-btn flat dense round icon="menu" @click="drawerOpen = !drawerOpen" class="lt-lg" />
        <q-toolbar-title class="text-body1 text-weight-medium">
          Panel de instructor
        </q-toolbar-title>
        <q-btn flat dense icon="arrow_back" label="Volver al sitio" to="/" no-caps />
      </q-toolbar>
    </q-header>

    <q-drawer v-model="drawerOpen" bordered show-if-above :width="240" class="bg-grey-1">
      <q-list class="q-pt-md">
        <q-item clickable to="/instructor/cursos" active-class="text-primary" exact>
          <q-item-section avatar><q-icon name="dashboard" /></q-item-section>
          <q-item-section>Mis cursos</q-item-section>
        </q-item>
        <q-item clickable to="/instructor/cursos/nuevo" active-class="text-primary">
          <q-item-section avatar><q-icon name="add_circle" /></q-item-section>
          <q-item-section>Crear curso</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const drawerOpen = ref(true)
</script>
