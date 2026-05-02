<template>
  <q-header bordered class="app-header">
    <q-toolbar :class="['app-toolbar', { 'app-toolbar--compact': compact }]">
      <!-- Botón mobile -->
      <q-btn
        flat
        dense
        round
        icon="menu"
        color="white"
        class="mobile-menu-btn"
        @click="$emit('toggleDrawer')"
      />

      <!-- Branding: el contenedor ocupa todo el ancho. El elemento interno arranca
           centrado (con flex justify-center) y al scrollear se desplaza a la izquierda
           (justify-start). Así el header NO cambia de altura y no hay huecos. -->
      <div class="brand-wrapper">
        <router-link to="/" class="app-brand">
          <img src="/logo.png" alt="CourselyLabs" class="app-brand__logo" />
          <span class="app-brand__text">
            <span class="app-brand__coursely">Coursely</span><span class="app-brand__labs">Labs</span>
          </span>
        </router-link>
      </div>
    </q-toolbar>
  </q-header>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const emit = defineEmits<{
  toggleDrawer: []
  'update:compact': [value: boolean]
}>()

const compact = ref(false)
const THRESHOLD = 60

function onScroll() {
  const next = window.scrollY > THRESHOLD
  if (next !== compact.value) {
    compact.value = next
    emit('update:compact', next)
  }
}

onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.app-header {
  background: var(--q-primary);
}

.app-toolbar {
  min-height: 64px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.mobile-menu-btn {
  flex: 0 0 auto;
}

@media (min-width: 1009px) {
  .mobile-menu-btn {
    display: none;
  }
}

/* Wrapper ocupa todo el ancho disponible y posiciona el branding */
.brand-wrapper {
  flex: 1 1 auto;
  display: flex;
  justify-content: center;
  transition: justify-content 0.3s ease;
}

/* Estado compacto: el branding se va a la izquierda. Solo en desktop —
   en mobile queda mejor centrado (la barra es más estrecha y
   moverlo a un lado deja un espacio raro). */
@media (min-width: 1009px) {
  .app-toolbar--compact .brand-wrapper {
    justify-content: flex-start;
  }
}

/* Branding (logo + texto) */
.app-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: white;
  transition: gap 0.3s ease;
}

.app-brand__logo {
  height: 44px;
  width: auto;
  transition: height 0.3s ease;
}

.app-brand__text {
  font-family: 'Monda', sans-serif;
  font-weight: 700;
  font-size: 1.5rem;
  letter-spacing: -0.5px;
  transition: font-size 0.3s ease;
}

.app-brand__coursely {
  color: var(--app-surface);
}

.app-brand__labs {
  color: var(--q-accent);
}

/* Estado compacto: logo y texto un poco más pequeños */
.app-toolbar--compact .app-brand__logo {
  height: 32px;
}

.app-toolbar--compact .app-brand__text {
  font-size: 1.25rem;
}

.app-toolbar--compact .app-brand {
  gap: 8px;
}
</style>
