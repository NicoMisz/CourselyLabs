<template>
  <q-layout view="lHh Lpr lFf">
    <q-header bordered class="bg-white text-dark">
      <q-toolbar>
        <q-btn flat dense round icon="menu" @click="drawerOpen = !drawerOpen" class="lt-lg" />
        <q-toolbar-title class="text-body1 text-weight-medium">
          {{ t('instructor.panelTitle') }}
        </q-toolbar-title>
        <q-btn flat dense icon="arrow_back" :label="t('common.backToSite')" to="/" no-caps />
      </q-toolbar>
    </q-header>

    <q-drawer v-model="drawerOpen" bordered show-if-above :width="240" class="bg-grey-1">
      <q-list class="q-pt-md">
        <q-item clickable to="/">
          <q-item-section avatar><q-icon name="arrow_back" /></q-item-section>
          <q-item-section>{{ t('common.backToSite') }}</q-item-section>
        </q-item>

        <q-separator class="q-my-sm" />

        <q-item clickable to="/instructor/cursos" active-class="text-primary" exact>
          <q-item-section avatar><q-icon name="dashboard" /></q-item-section>
          <q-item-section>{{ t('instructor.myCourses') }}</q-item-section>
        </q-item>

        <q-item clickable to="/instructor/calificar" active-class="text-primary" exact>
          <q-item-section avatar><q-icon name="assignment_turned_in" /></q-item-section>
          <q-item-section>{{ t('nav.grading') }}</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const drawerOpen = ref(true)
</script>
