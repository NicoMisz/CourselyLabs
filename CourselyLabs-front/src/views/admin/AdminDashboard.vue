<template>
  <q-page class="q-pa-lg">
    <h1 class="text-h5 q-mb-lg">Panel de administración</h1>

    <div v-if="loading" class="row q-gutter-md">
      <div v-for="i in 4" :key="i" class="col-12 col-sm-6 col-md-3">
        <q-skeleton type="rect" height="100px" />
      </div>
    </div>

    <div v-else class="row q-gutter-md">
      <div class="col-12 col-sm-6 col-md-3">
        <q-card flat bordered class="q-pa-md">
          <div class="text-caption text-grey-7">Usuarios totales</div>
          <div class="text-h4 text-weight-bold">{{ stats.totalUsers }}</div>
          <q-icon name="people" size="28px" color="primary" class="q-mt-xs" />
        </q-card>
      </div>
      <div class="col-12 col-sm-6 col-md-3">
        <q-card flat bordered class="q-pa-md">
          <div class="text-caption text-grey-7">Cursos publicados</div>
          <div class="text-h4 text-weight-bold">{{ stats.publishedCourses }}</div>
          <q-icon name="school" size="28px" color="positive" class="q-mt-xs" />
        </q-card>
      </div>
      <div class="col-12 col-sm-6 col-md-3">
        <q-card flat bordered class="q-pa-md">
          <div class="text-caption text-grey-7">Cursos pendientes</div>
          <div class="text-h4 text-weight-bold" :class="stats.pendingCourses > 0 ? 'text-warning' : ''">{{ stats.pendingCourses }}</div>
          <q-icon name="rate_review" size="28px" color="warning" class="q-mt-xs" />
        </q-card>
      </div>
      <div class="col-12 col-sm-6 col-md-3">
        <q-card flat bordered class="q-pa-md">
          <div class="text-caption text-grey-7">Inscripciones totales</div>
          <div class="text-h4 text-weight-bold">{{ stats.totalEnrollments }}</div>
          <q-icon name="how_to_reg" size="28px" color="accent" class="q-mt-xs" />
        </q-card>
      </div>
    </div>

    <!-- Quick links -->
    <div class="row q-gutter-md q-mt-lg">
      <q-btn v-if="stats.pendingCourses > 0" outline color="warning" icon="rate_review" :label="`Revisar ${stats.pendingCourses} curso(s) pendiente(s)`" to="/admin/cursos" no-caps />
      <q-btn outline color="primary" icon="people" label="Gestionar usuarios" to="/admin/usuarios" no-caps />
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { getAdminStats } from '../../api/admin'
import type { AdminStats } from '../../api/admin'

const $q = useQuasar()
const loading = ref(true)
const stats = ref<AdminStats>({ totalUsers: 0, publishedCourses: 0, pendingCourses: 0, totalEnrollments: 0 })

onMounted(async () => {
  try {
    stats.value = await getAdminStats()
  } catch {
    $q.notify({ type: 'negative', message: 'Error al cargar estadisticas', position: 'bottom-right' })
  } finally {
    loading.value = false
  }
})
</script>
