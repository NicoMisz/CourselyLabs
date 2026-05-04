<template>
  <q-layout view="lHh Lpr lFf" :class="$q.dark.isActive ? 'bg-grey-10' : 'bg-grey-1'">
    <q-header bordered :class="$q.dark.isActive ? 'bg-grey-9 text-white' : 'bg-white text-dark'">
      <q-toolbar>
        <q-btn flat dense round icon="menu" @click="drawerOpen = !drawerOpen" class="lt-lg" />
        <q-toolbar-title class="text-body1 text-weight-medium">
          Administración
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="drawerOpen" bordered show-if-above :width="240" :class="$q.dark.isActive ? 'bg-grey-9' : 'bg-grey-1'">
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
        <q-item clickable to="/admin/categorias" active-class="text-primary">
          <q-item-section avatar><q-icon name="category" /></q-item-section>
          <q-item-section>Categorías</q-item-section>
        </q-item>

        <q-separator class="q-my-sm" />

        <q-item clickable @click="toggleDark">
          <q-item-section avatar>
            <q-icon :name="$q.dark.isActive ? 'light_mode' : 'dark_mode'" />
          </q-item-section>
          <q-item-section>{{ $q.dark.isActive ? 'Modo claro' : 'Modo oscuro' }}</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useQuasar } from 'quasar'
import { getPendingCourses } from '../api/admin'

const $q = useQuasar()
const drawerOpen = ref(true)
const pendingCount = ref(0)

function toggleDark() {
  const next = !$q.dark.isActive
  $q.dark.set(next)
  localStorage.setItem('coursely-dark', next ? '1' : '0')
}

async function refreshPending() {
  try {
    const courses = await getPendingCourses()
    pendingCount.value = courses.length
  } catch { /* ignore */ }
}

// Otros componentes (p. ej. AdminCourseQueue) emiten este evento tras aprobar/rechazar
// para que el badge del sidebar se actualice sin recargar la página.
function handleRefreshEvent() {
  refreshPending()
}

onMounted(() => {
  refreshPending()
  window.addEventListener('admin:refresh-pending', handleRefreshEvent)
})
onBeforeUnmount(() => {
  window.removeEventListener('admin:refresh-pending', handleRefreshEvent)
})
</script>
