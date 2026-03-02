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
      <q-btn flat round dense icon="account_circle" @click="$router.push('/login')"/>
    </q-toolbar>
  </q-header>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  showMenuButton?: boolean
}>(), {
  showMenuButton: true,
})

defineEmits<{
  toggleDrawer: []
}>()
</script>
