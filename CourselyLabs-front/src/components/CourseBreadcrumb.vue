<template>
  <q-breadcrumbs class="text-grey-8 q-mb-md">
    <q-breadcrumbs-el label="Home" icon="home" to="/" />
    <q-breadcrumbs-el label="Cursos" to="/cursos" />
    <q-breadcrumbs-el v-if="categoryName" :label="categoryName" />
    <q-breadcrumbs-el :label="title" />
  </q-breadcrumbs>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  categoryName?: string;
}>();
</script>