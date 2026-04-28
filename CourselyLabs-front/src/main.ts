// src/main.ts
import './css/app.scss'  // Añade esta línea al inicio

import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { Quasar, Notify } from 'quasar'

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
  plugins: { Notify },
})

// Verify sesión on app startup (refresh token if needed, clear if invalid)
import { useAuthStore } from './stores/auth'
const authStore = useAuthStore()
authStore.checkSession().finally(() => {
  app.mount('#app')
})
