<template>
  <q-page class="flex flex-center">
    <div class="text-center q-pa-xl" style="max-width: 500px">
      <div class="success-icon q-mx-auto q-mb-lg">
        <q-icon name="check" size="48px" color="white" />
      </div>
      <h1 class="text-h5 q-mb-sm">Bienvenido a Premium!</h1>
      <p class="text-body1 text-grey-7 q-mb-lg">
        Tu suscripcion se ha activado correctamente. Ya tienes acceso a todos los cursos premium.
      </p>
      <div class="row q-gutter-md justify-center">
        <q-btn color="primary" label="Explorar cursos" to="/cursos" unelevated no-caps />
        <q-btn outline color="primary" label="Ir a mi perfil" to="/profile" no-caps />
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  // Refresh session to get updated role
  authStore.checkSession()
})
</script>

<style scoped>
.success-icon {
  width: 88px;
  height: 88px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #059669, #0f766e);
}
</style>
