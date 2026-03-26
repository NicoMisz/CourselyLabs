<template>
  <q-header
    :class="['app-header', { 'app-header--hidden': hidden }]"
    bordered
  >
    <q-toolbar class="app-toolbar">
      <q-btn
        flat
        dense
        round
        icon="menu"
        color="white"
        class="mobile-menu-btn"
        @click="$emit('toggleDrawer')"
      />

      <q-space />

      <router-link to="/" class="app-logo">
        <span class="app-logo__coursely">Coursely</span><span class="app-logo__labs">Labs</span>
      </router-link>

      <q-space />

      <div class="mobile-menu-spacer" style="width: 40px" />
    </q-toolbar>
  </q-header>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const emit = defineEmits<{
  toggleDrawer: []
  'update:hidden': [value: boolean]
}>()

const hidden = ref(false)
const THRESHOLD = 80

function onScroll() {
  const nowHidden = window.scrollY > THRESHOLD
  if (nowHidden !== hidden.value) {
    hidden.value = nowHidden
    emit('update:hidden', nowHidden)
  }
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.app-header {
  background: #0f766e;
  transition: transform 0.35s ease;
}

.app-header--hidden {
  transform: translateY(-100%);
}

.app-toolbar {
  min-height: 64px;
}

.app-logo {
  text-decoration: none;
  font-family: 'Monda', sans-serif;
  font-weight: 700;
  font-size: 1.6rem;
  letter-spacing: -0.5px;
}

.app-logo__coursely {
  color: #ffffff;
}

.app-logo__labs {
  color: #ea580c;
}

@media (min-width: 1009px) {/* Por culpa del minisidebar es este numero tan raro */
  .mobile-menu-btn,
  .mobile-menu-spacer {
    display: none !important;
  }
}
</style>
