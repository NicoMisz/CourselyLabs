// src/main.ts
import './css/app.scss'  // Añade esta línea al inicio

import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { Quasar, Notify, Dark } from 'quasar'

// Import icon libraries
import '@quasar/extras/material-icons/material-icons.css'

// Import Quasar css
import 'quasar/src/css/index.sass'

import App from './App.vue'
import router from './router'
import i18n from './i18n'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(i18n)
app.use(Quasar, {
  plugins: { Notify, Dark },
})

// Restaura la preferencia de modo oscuro guardada en localStorage.
// El toggle vive en AppSidebar.vue (Bloque 4 del fix/ux-pass-1).
const savedDark = localStorage.getItem('coursely-dark')
if (savedDark !== null) {
  Dark.set(savedDark === '1')
}

// Verify sesión on app startup (refresh token if needed, clear if invalid)
import { useAuthStore } from './stores/auth'
const authStore = useAuthStore()
authStore.checkSession().finally(() => {
  app.mount('#app')
})
