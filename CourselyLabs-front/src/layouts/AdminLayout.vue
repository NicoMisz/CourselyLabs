<template>
  <q-layout view="lHh Lpr lFf">
    <q-header bordered class="bg-white text-dark">
      <q-toolbar>
        <q-btn flat dense round icon="menu" @click="drawerOpen = !drawerOpen" class="lt-lg" />
        <q-toolbar-title class="text-body1 text-weight-medium">
          Administracion
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="drawerOpen" bordered show-if-above :width="240" class="bg-grey-1">
      <q-list class="q-pt-md">
        <q-item clickable to="/">
          <q-item-section avatar><q-icon name="arrow_back" /></q-item-section>
          <q-item-section>Volver al sitio</q-item-section>
        </q-item>

        <q-separator class="q-my-sm" />

        <q-item clickable to="/admin" active-class="text-primary" exact>
          <q-item-section avatar><q-icon name="dashboard" /></q-item-section>
          <q-item-section>Panel</q-item-section>
        </q-item>
        <q-item clickable to="/admin/cursos" active-class="text-primary">
          <q-item-section avatar>
            <q-icon name="rate_review" />
          </q-item-section>
          <q-item-section>
            Cursos pendientes
          </q-item-section>
          <q-item-section side v-if="pendingCount > 0">
            <q-badge color="warning" :label="pendingCount" />
          </q-item-section>
        </q-item>
        <q-item clickable to="/admin/usuarios" active-class="text-primary">
          <q-item-section avatar><q-icon name="people" /></q-item-section>
          <q-item-section>Usuarios</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPendingCourses } from '../api/admin'

const drawerOpen = ref(true)
const pendingCount = ref(0)

onMounted(async () => {
  try {
    const courses = await getPendingCourses()
    pendingCount.value = courses.length
  } catch { /* ignore */ }
})
</script>
