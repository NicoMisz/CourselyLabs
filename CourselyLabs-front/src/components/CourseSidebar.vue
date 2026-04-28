<template>
  <aside class="course-sidebar q-pa-md rounded-borders">
    <div class="text-subtitle1 text-weight-medium q-mb-md">Resumen</div>

    <div class="q-gutter-y-sm">
      <div><strong>Nivel:</strong> {{ level || 'Sin nivel' }}</div>
      <div><strong>Duracion:</strong> {{ durationText || 'Proximamente' }}</div>
      <div><strong>Estudiantes:</strong> {{ studentsCount || 0 }}</div>
      <div><strong>Valoracion:</strong> {{ averageRating?.toFixed(1) || 'N/A' }}</div>
    </div>

    <PrerequisiteBlockBanner
      v-if="prerequisiteBlockers?.length"
      :blockers="prerequisiteBlockers"
      @open-related-tab="$emit('open-related-tab')"
    />

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
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import type { BlockedPrerequisite } from '@/types/prerequisite'
import PrerequisiteBlockBanner from './PrerequisiteBlockBanner.vue'

// Props
const props = defineProps<{
  level?: string
  durationText?: string
  studentsCount?: number
  averageRating?: number
  courseId?: string
  courseSlug?: string
  isFree: boolean
  price?: number
  enrolled: boolean
  loading?: boolean
  prerequisiteBlockers?: BlockedPrerequisite[]
}>()

// Emits
const emit = defineEmits<{
  (e: 'enroll'): void
  (e: 'continue'): void
  (e: 'open-related-tab'): void
}>()

// variables y stores necesarios para la lógica del botón de acción principal
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// Variables para controlar el estado del botón de acción principal
const isGuest = computed(() => !authStore.isLoggedIn)
const isUserPremium = computed(() => authStore.user?.role === 'premium')
const hasBlockedPrerequisites = computed(() => (props.prerequisiteBlockers?.length ?? 0) > 0)

const buttonDisabled = computed(() => hasBlockedPrerequisites.value)

// Computed properties para determinar el label, color e icono del botón de acción principal
const buttonLabel = computed(() => {
  if (isGuest.value) return 'Inicia sesion para inscribirte'
  if (props.enrolled) return 'Continuar curso'
  if (props.isFree) return 'Inscribirme gratis'
  if (!isUserPremium.value) return 'Hazte Premium'
  return 'Inscribirme'
});

// El color del botón es principal si el usuario ya está inscrito o si el curso es gratuito, 
// de lo contrario es ámbar si el usuario no es premium, o principal si lo es (para cursos premium)
const buttonColor = computed(() => {
  if (props.enrolled) return 'primary'
  if (props.isFree) return 'positive'
  return isUserPremium.value ? 'primary' : 'amber-8'
});

// El icono del botón cambia según el estado del usuario y del curso:
const buttonIcon = computed(() => {
  if (isGuest.value) return 'login'
  if (props.enrolled) return 'play_circle'
  if (props.isFree) return 'check_circle'
  return isUserPremium.value ? 'workspace_premium' : 'workspace_premium'
});

// Maneja el click en el botón de acción principal, redirigiendo o emitiendo eventos según corresponda
function handleClick() {
  // Si el usuario es invitado, lo redirige a la página de login con una query para volver al curso después de iniciar sesión
  if (isGuest.value) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  // Si el usuario ya está inscrito, emite el evento para continuar el curso
  if (props.enrolled) {
    emit('continue')
    return
  }

  // Si el usuario no cumple con los prerrequisitos, no hace nada (el botón estará deshabilitado)
  if (hasBlockedPrerequisites.value) {
    return
  }

  // Si el curso es gratuito, emite el evento para inscribirse sin necesidad de ser premium
  if (props.isFree) {
    emit('enroll')
    return
  }

  // Si el curso es premium y el usuario no es premium, lo redirige a la página de premium
  if (!isUserPremium.value) {
    router.push('/premium')
    return
  }

  // Si el curso es premium y el usuario es premium, emite el evento para inscribirse
  emit('enroll')
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