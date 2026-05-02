<template>
  <q-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    show-if-above
    bordered
    :class="$q.dark.isActive ? 'bg-grey-9' : 'bg-grey-1'"
    :mini="mini"
    @mouseenter="$emit('update:mini', false)"
    @mouseleave="$emit('update:mini', true)"
    :width="250"
    :mini-width="60"
  >
    <q-list class="q-pt-sm">
      <!-- Navegación principal -->
      <SidebarItem
        v-for="link in navLinks"
        :key="link.link"
        v-bind="link"
        :mini="mini"
      />

      <q-separator class="q-my-sm" />

      <!-- Toggle modo oscuro -->
      <q-item clickable @click="toggleDark">
        <q-item-section avatar>
          <q-icon :name="$q.dark.isActive ? 'light_mode' : 'dark_mode'" />
          <q-tooltip v-if="mini" anchor="center right" self="center left" :offset="[10, 0]">
            {{ $q.dark.isActive ? 'Modo claro' : 'Modo oscuro' }}
          </q-tooltip>
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $q.dark.isActive ? 'Modo claro' : 'Modo oscuro' }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator class="q-my-sm" />

      <!-- Autenticado -->
      <template v-if="authStore.isLoggedIn">
        <SidebarItem
          v-for="link in authLinks"
          :key="link.link"
          v-bind="link"
          :mini="mini"
        />

        <q-separator class="q-my-sm" />

        <!-- Premium upsell -->
        <SidebarItem
          v-if="!isPremiumOrAdmin"
          title="Hazte Premium"
          icon="workspace_premium"
          link="/premium"
          :mini="mini"
        />

        <!-- Usuario -->
        <q-item clickable to="/profile" active-class="text-primary">
          <q-item-section avatar>
            <q-avatar size="32px" :color="isPremiumOrAdmin ? 'amber-8' : 'primary'" text-color="white" font-size="14px">
              {{ initials }}
            </q-avatar>
            <q-tooltip v-if="mini" anchor="center right" self="center left" :offset="[10, 0]">
              Mi perfil
            </q-tooltip>
          </q-item-section>
          <q-item-section>
            <q-item-label>
              {{ authStore.user?.firstName }} {{ authStore.user?.lastName }}
              <q-icon v-if="isPremiumOrAdmin" name="workspace_premium" color="amber-8" size="16px" class="q-ml-xs" />
            </q-item-label>
            <q-item-label caption>{{ rolLabel }}</q-item-label>
          </q-item-section>
        </q-item>

        <q-item clickable @click="handleLogout">
          <q-item-section avatar>
            <q-icon name="logout" />
            <q-tooltip v-if="mini" anchor="center right" self="center left" :offset="[10, 0]">
              Cerrar sesión
            </q-tooltip>
          </q-item-section>
          <q-item-section>
            <q-item-label>Cerrar sesión</q-item-label>
          </q-item-section>
        </q-item>
      </template>

      <!-- No autenticado -->
      <template v-else>
        <SidebarItem title="Iniciar sesión" icon="login" link="/login" :mini="mini" />
        <SidebarItem title="Registrarse" icon="person_add" link="/register" :mini="mini" />
      </template>
    </q-list>
  </q-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useQuasar } from 'quasar'
import { useAuthStore } from '../stores/auth'
import SidebarItem from './SidebarItem.vue'

defineProps<{
  modelValue: boolean
  mini: boolean
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
  'update:mini': [value: boolean]
}>()

const $q = useQuasar()
const authStore = useAuthStore()
const router = useRouter()

function toggleDark() {
  const next = !$q.dark.isActive
  $q.dark.set(next)
  localStorage.setItem('coursely-dark', next ? '1' : '0')
}

const initials = computed(() => {
  const f = authStore.user?.firstName?.[0] || ''
  const l = authStore.user?.lastName?.[0] || ''
  return (f + l).toUpperCase()
})

const rolLabel = computed(() => {
  switch (authStore.user?.role) {
    case 'admin': return 'Administrador'
    case 'premium': return 'Premium'
    default: return 'Estudiante'
  }
})

const isPremiumOrAdmin = computed(() =>
  authStore.user?.role === 'premium' || authStore.user?.role === 'admin'
)

const navLinks = [
  { title: 'Inicio', icon: 'home', link: '/', exact: true },
  { title: 'Cursos', icon: 'school', link: '/cursos' },
]

const authLinks = computed(() => {
  const links = [
    { title: 'Cursos inscritos', icon: 'menu_book', link: '/mis-cursos' },
    { title: 'Cursos creados', icon: 'edit_note', link: '/instructor/cursos' },
  ]
  if (authStore.user?.role === 'admin') {
    links.push({ title: 'Administración', icon: 'admin_panel_settings', link: '/admin' })
  }
  return links
})

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

