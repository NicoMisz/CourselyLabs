<template>
  <aside class="course-sidebar q-pa-md rounded-borders">
    <div class="text-subtitle1 text-weight-medium q-mb-md">Resumen</div>

    <div class="q-gutter-y-sm">
      <div><strong>Nivel:</strong> {{ level || 'Sin nivel' }}</div>
      <div><strong>Duracion:</strong> {{ durationText || 'Proximamente' }}</div>
      <div><strong>Estudiantes:</strong> {{ studentsCount || 0 }}</div>
      <div><strong>Valoracion:</strong> {{ averageRating?.toFixed(1) || 'N/A' }}</div>
    </div>

    <q-btn
      class="q-mt-lg full-width"
      color="accent"
      label="Inscribirme"
      icon="how_to_reg"
      unelevated
    />
  </aside>
</template>

<script setup lang="ts">
defineProps<{
  level?: string;
  durationText?: string;
  studentsCount?: number;
  averageRating?: number;
}>();
</script>

<style scoped>
.course-sidebar {
  position: sticky;
  top: 80px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: white;
}
@media (max-width: 767px) {
  .course-sidebar {
    position: static;
  }
}
</style>