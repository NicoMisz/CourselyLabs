<template>
  <q-page class="q-pa-md q-pa-lg-lg legal-page">
    <div class="legal-wrap">
      <div class="text-center q-mb-lg">
        <q-icon name="help_center" size="42px" color="primary" />
        <h1 class="text-h4 q-mt-sm q-mb-xs page-title">Preguntas frecuentes</h1>
        <p class="text-body2 page-subtitle">Las dudas más habituales sobre cómo funciona CourselyLabs.</p>
      </div>

      <div v-for="cat in categories" :key="cat.title" class="q-mb-xl">
        <h2 class="text-h6 q-mb-md cat-title">{{ cat.title }}</h2>
        <q-card flat bordered>
          <q-expansion-item
            v-for="(faq, i) in cat.items"
            :key="faq.q"
            :label="faq.q"
            header-class="text-weight-medium text-body1"
            :default-opened="i === 0"
          >
            <q-card flat>
              <q-card-section class="text-body2 q-faq-answer" v-html="faq.a" />
            </q-card>
            <q-separator />
          </q-expansion-item>
        </q-card>
      </div>

      <div class="text-center q-mt-xl">
        <p class="text-body2 text-grey-7">¿No encuentras lo que buscas?</p>
        <q-btn unelevated color="primary" icon="mail" label="Contactar con soporte" no-caps to="/contacto" />
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
interface Faq { q: string; a: string }
interface FaqCategory { title: string; items: Faq[] }

const categories: FaqCategory[] = [
  {
    title: 'Cuenta y acceso',
    items: [
      { q: '¿Cómo creo una cuenta?', a: 'Pulsa en <router-link to="/register">Registrarse</router-link>, introduce tu email y contraseña y recibirás un email para verificar tu cuenta.' },
      { q: 'No me llega el email de verificación', a: 'Revisa la carpeta de spam. Si no aparece, desde tu perfil puedes pulsar «Reenviar email de verificación». Si sigue sin llegar, contacta con soporte.' },
      { q: 'He olvidado mi contraseña', a: 'En la página de inicio de sesión, pulsa «¿Olvidaste tu contraseña?» y te enviaremos un enlace para restablecerla. El enlace expira en 1 hora.' },
      { q: '¿Puedo cambiar mi email?', a: 'Aún no, pero está en el roadmap. Mientras tanto, escríbenos a soporte y te ayudamos.' },
    ],
  },
  {
    title: 'Cursos y aprendizaje',
    items: [
      { q: '¿Cuánto tiempo tengo para completar un curso?', a: 'El acceso es ilimitado mientras tengas la cuenta activa (o tu suscripción Premium activa para cursos Premium). Aprende a tu ritmo.' },
      { q: '¿Qué pasa con mi progreso si dejo de pagar?', a: 'Tu progreso se conserva. Si vuelves a suscribirte, lo retomas donde lo dejaste.' },
      { q: '¿Puedo descargar los vídeos?', a: 'No. Los vídeos se reproducen en streaming desde la plataforma para proteger el contenido de los instructores.' },
      { q: '¿Cómo me certifico al terminar un curso?', a: 'Los certificados están en el roadmap. Por ahora puedes ver tu progreso del 100 % en «Mis cursos».' },
    ],
  },
  {
    title: 'Laboratorios virtuales',
    items: [
      { q: '¿Qué son los laboratorios?', a: 'Son entornos Linux reales que se ejecutan en nuestra infraestructura y a los que conectas desde la propia lección, sin instalar nada en tu equipo.' },
      { q: '¿Necesito instalar algo en mi ordenador?', a: 'No. La consola se abre dentro del navegador (noVNC). Solo necesitas un navegador moderno.' },
      { q: 'Mi laboratorio dice «No tienes una VM asignada»', a: 'Significa que el instructor del curso aún no te ha aprovisionado una máquina. Avísale para que lo haga; tras eso, refresca la lección.' },
      { q: '¿Mi VM se queda encendida siempre?', a: 'No. La VM se apaga cuando pulsas «Detener» y, en el futuro, también automáticamente por inactividad.' },
    ],
  },
  {
    title: 'Suscripción Premium',
    items: [
      { q: '¿Puedo cancelar en cualquier momento?', a: 'Sí, puedes cancelar tu suscripción cuando quieras desde tu perfil. Mantendrás acceso hasta el final del periodo de facturación.' },
      { q: '¿Qué pasa con mis cursos si cancelo?', a: 'Perderás acceso a los cursos Premium. Los cursos gratuitos, tu progreso y los cursos que hayas creado se mantienen.' },
      { q: '¿Los pagos son seguros?', a: 'Sí. Todos los pagos se procesan a través de Stripe, una de las plataformas de pago más seguras y usadas del mundo. Nunca almacenamos los datos de tu tarjeta en nuestros servidores.' },
      { q: '¿Puedo cambiar de plan mensual a anual?', a: 'Sí. Contacta con soporte y te ayudaremos a cambiar de plan sin perder días pagados.' },
      { q: '¿Hay reembolsos?', a: 'No ofrecemos reembolsos automáticos. Si tienes un problema concreto, contacta con soporte y lo evaluamos caso por caso.' },
    ],
  },
  {
    title: 'Para instructores',
    items: [
      { q: '¿Quién puede crear cursos?', a: 'Cualquier usuario verificado puede crear cursos. Los usuarios estándar pueden crear hasta 2 cursos; los Premium hasta 10; los administradores sin límite.' },
      { q: '¿Mis cursos están limitados en almacenamiento?', a: 'Sí. Los usuarios estándar tienen 300 MB por curso para vídeos, PDFs y recursos. Premium tiene 1 GB, y los administradores ilimitado.' },
      { q: '¿Cómo se publica un curso?', a: 'Desde el editor pulsa «Solicitar revisión». Un administrador revisa el curso y lo aprueba o rechaza con motivo. Tras aprobarse, queda público en el catálogo.' },
      { q: '¿Puedo tener varios instructores en un curso?', a: 'El modelo de datos lo soporta. La UI para invitar co-instructores está prevista en el roadmap.' },
    ],
  },
]
</script>

<style scoped>
.legal-page {
  background: var(--app-bg);
  color: var(--app-text);
}
.legal-wrap {
  max-width: 860px;
  margin: 0 auto;
}
.page-title {
  font-family: 'Monda', sans-serif;
  color: var(--app-text-strong);
}
.page-subtitle {
  color: var(--app-text-soft);
}
.cat-title {
  font-family: 'Monda', sans-serif;
  color: var(--app-text-strong);
}
.q-faq-answer {
  color: var(--app-text-soft);
}
.q-faq-answer :deep(a) {
  color: var(--q-primary);
  text-decoration: none;
}
.q-faq-answer :deep(a:hover) {
  text-decoration: underline;
}
</style>
