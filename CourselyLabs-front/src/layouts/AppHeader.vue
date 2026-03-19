<template>
  <q-header>
    <q-toolbar>

      <q-toolbar-title class="cursor-pointer" @click="$router.push('/')">
        CourselyLabs
      </q-toolbar-title>

      <q-space />

      <q-btn flat no-caps label="Cursos" @click="$router.push('/cursos')" />

      <template v-if="authStore.isLoggedIn">
        <q-btn flat no-caps label="Mis cursos" @click="$router.push('/mis-cursos')" />
        <span class="q-mx-sm text-body2">{{ authStore.user?.firstName }}</span>
        <q-btn flat round dense icon="account_circle" @click="$router.push('/profile')" />
        <q-btn flat round dense icon="logout" @click="handleLogout" />
      </template>

      <template v-else>
        <q-btn flat no-caps label="Iniciar sesion" @click="$router.push('/login')" />
      </template>
    </q-toolbar>
  </q-header>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';

withDefaults(defineProps<{ showMenuButton?: boolean }>(), {
  showMenuButton: true,
});

defineEmits<{ toggleDrawer: [] }>();

const authStore = useAuthStore();
const router = useRouter();

async function handleLogout() {
  await authStore.logout();
  router.push('/login');
}
</script>
