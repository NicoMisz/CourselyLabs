<template>
  <q-footer class="app-footer" :reveal="false" bordered>
    <div class="footer-content">
      <!-- Branding -->
      <div class="footer-branding">
        <img src="/logo.png" alt="CourselyLabs" class="footer-logo" />
        <span class="footer-brand-text">
          <span class="footer-brand__coursely">Coursely</span><span class="footer-brand__labs">Labs</span>
        </span>
      </div>

      <!-- Columnas de links -->
      <div class="footer-columns">
        <div class="footer-column">
          <div class="footer-column__title">Producto</div>
          <ul class="footer-column__list">
            <li><router-link to="/cursos">Cursos</router-link></li>
            <li><router-link to="/premium">Premium</router-link></li>
            <li><router-link to="/mis-cursos">Mis cursos</router-link></li>
          </ul>
        </div>

        <div class="footer-column">
          <div class="footer-column__title">Legal</div>
          <ul class="footer-column__list">
            <li><router-link to="/terminos">Términos</router-link></li>
            <li><router-link to="/proximamente">Privacidad</router-link></li>
            <li><router-link to="/proximamente">Cookies</router-link></li>
          </ul>
        </div>

        <div class="footer-column">
          <div class="footer-column__title">Soporte</div>
          <ul class="footer-column__list">
            <li><router-link to="/proximamente">Contacto</router-link></li>
            <li><router-link to="/proximamente">FAQ</router-link></li>
            <li><router-link to="/proximamente">Estado del servicio</router-link></li>
          </ul>
        </div>
      </div>
    </div>

    <div class="footer-copyright">
      © {{ year }} CourselyLabs. Todos los derechos reservados.
    </div>
  </q-footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const year = computed(() => new Date().getFullYear())
</script>

<style scoped>
/* Footer siempre oscuro, en ambos modos. En modo oscuro lo bajamos un escalón
   para que contraste con el fondo. */
.app-footer {
  background: #1f2937;
  color: #d1d5db;
  padding: 32px 24px 12px;
}
:global(.body--dark) .app-footer {
  background: #0a0a0a;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  align-items: flex-start;
  justify-content: space-between;
}

.footer-branding {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 0 0 auto;
}

.footer-logo {
  height: 40px;
  width: auto;
}

.footer-brand-text {
  font-family: 'Monda', sans-serif;
  font-weight: 700;
  font-size: 1.25rem;
}

.footer-brand__coursely {
  color: #ffffff;
}

.footer-brand__labs {
  color: var(--q-accent);
}

.footer-columns {
  display: flex;
  flex-wrap: wrap;
  gap: 48px;
}

.footer-column__title {
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 8px;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.footer-column__list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-column__list li {
  margin-bottom: 6px;
}

.footer-column__list a {
  color: #d1d5db;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.15s ease;
}

.footer-column__list a:hover {
  color: #14b8a6;
}

.footer-copyright {
  max-width: 1200px;
  margin: 24px auto 0;
  padding-top: 16px;
  border-top: 1px solid #374151;
  text-align: center;
  font-size: 0.8rem;
  color: #9ca3af;
}

@media (max-width: 600px) {
  .footer-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
  }
  .footer-columns {
    flex-direction: column;
    gap: 24px;
  }
}
</style>
