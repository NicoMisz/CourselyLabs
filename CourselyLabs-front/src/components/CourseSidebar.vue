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
      :label="buttonLabel"
      :color="buttonColor"
      :outline="isGuest"
      :loading="loading"
      :icon="buttonIcon"
      :disable="buttonDisabled"
      unelevated
      no-caps
      class="full-width q-mt-md"
      @click="handleClick"
    />
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';

import type { BlockedPrerequisite } from '@/types/prerequisite'

const props = defineProps<{
  level?: string;
  durationText?: string;
  studentsCount?: number;
  averageRating?: number;
  courseId?: string;
  courseSlug?: string;
  isFree: boolean;
  price?: number;
  enrolled: boolean;
  loading?: boolean;
  prerequisiteBlockers?: BlockedPrerequisite[]
}>();

const emit = defineEmits<{
  enroll: [];
  continue: [];
}>();

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const isGuest = computed(() => !authStore.isLoggedIn);

const isPaid = computed(() => !props.isFree);

const hasBlockedPrerequisites = computed(() => (props.prerequisiteBlockers?.length ?? 0) > 0);

const buttonLabel = computed(() => {
  if (isGuest.value) return 'Inicia sesion para inscribirte';
  if (props.enrolled) return 'Continuar curso';
  if (props.isFree) return 'Inscribirme gratis';
  return 'Proximamente';
});

const buttonColor = computed(() => {
  if (props.enrolled) return 'primary';
  if (props.isFree) return 'positive';
  return 'grey-5';
});

const buttonIcon = computed(() => {
  if (isGuest.value) return 'login';
  if (props.enrolled) return 'play_circle';
  if (props.isFree) return 'check_circle';
  return 'lock';
});

const buttonDisabled = computed(() => {
  if (hasBlockedPrerequisites.value) return true;
  return isPaid.value && !props.enrolled && !isGuest.value;
});

function handleClick() {
  if (isGuest.value) {
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }
  if (props.enrolled) {
    emit('continue');
    return;
  }
  if (props.isFree) {
    emit('enroll');
    return;
  }
}
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