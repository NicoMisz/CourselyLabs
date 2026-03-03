<template>
  <q-header>
    <q-toolbar>
      <q-btn
        v-if="showMenuButton"
        flat
        dense
        round
        @click="$emit('toggleDrawer')"
        aria-label="Menu"
        icon="menu"
      />

      <q-toolbar-title>CourselyLabs</q-toolbar-title>

      <template v-if="authStore.isLoggedIn">
        <span class="q-mr-sm text-body2">{{ authStore.user?.firstName }}</span>
        <q-btn flat round dense icon="account_circle" @click="$router.push('/profile')" />
        <q-btn flat round dense icon="logout" @click="handleLogout" />
      </template>

      <template v-else>
        <q-btn flat round dense icon="account_circle" @click="$router.push('/login')" />
      </template>
    </q-toolbar>
  </q-header>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

withDefaults(defineProps<{
  showMenuButton?: boolean
}>(), {
  showMenuButton: true,
})

defineEmits<{
  toggleDrawer: []
}>()

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>
