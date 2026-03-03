<template>
  <q-page class="flex justify-center items-start q-pt-xl">
    <q-card class="profile-card q-pa-lg">
      <q-card-section class="text-center">
        <q-avatar size="80px" color="primary" text-color="white" class="q-mb-md">
          <q-icon name="account_circle" size="60px" />
        </q-avatar>
        <div class="text-h5">{{ user?.firstName }} {{ user?.lastName }}</div>
        <div class="text-subtitle2 text-grey q-mt-xs">{{ user?.email }}</div>
        <q-badge :color="roleColor" class="q-mt-sm">{{ roleLabel }}</q-badge>
      </q-card-section>

      <q-separator />

      <q-card-section v-if="user?.bio">
        <div class="text-caption text-grey q-mb-xs">Sobre mí</div>
        <p>{{ user.bio }}</p>
      </q-card-section>

      <q-card-section>
        <div class="text-caption text-grey">
          Cuenta verificada: {{ user?.isVerified ? 'Sí' : 'No' }}
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const roleLabel = computed(() => {
  switch (user.value?.role) {
    case 'admin': return 'Administrador'
    case 'premium': return 'Premium'
    default: return 'Usuario'
  }
})

const roleColor = computed(() => {
  switch (user.value?.role) {
    case 'admin': return 'negative'
    case 'premium': return 'warning'
    default: return 'primary'
  }
})
</script>

<style scoped>
.profile-card {
  width: 100%;
  max-width: 500px;
}
</style>
