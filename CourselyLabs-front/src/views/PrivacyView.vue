<template>
  <q-page class="q-pa-md q-pa-lg-lg legal-page">
    <div class="legal-wrap">
      <div class="text-center q-mb-lg">
        <q-icon name="privacy_tip" size="42px" color="primary" />
        <h1 class="text-h4 q-mt-sm q-mb-xs page-title">Política de privacidad</h1>
        <p class="text-body2 page-subtitle">Última actualización: {{ updatedAt }}</p>
      </div>

      <q-banner class="bg-orange-1 text-orange-10 q-mb-lg" rounded dense>
        <template v-slot:avatar>
          <q-icon name="gavel" color="orange-9" />
        </template>
        Este texto es una plantilla genérica orientada al RGPD; sustitúyelo por la política redactada por tu asesor legal antes de operar comercialmente.
      </q-banner>

      <section class="prose">
        <h2>1. Responsable del tratamiento</h2>
        <p>
          CourselyLabs es responsable del tratamiento de tus datos personales. Para cualquier cuestión relativa a la protección de datos puedes escribirnos a <a href="mailto:privacidad@courselylabs.com">privacidad@courselylabs.com</a>.
        </p>

        <h2>2. Qué datos recopilamos</h2>
        <ul>
          <li><strong>Datos de cuenta</strong>: email, nombre, apellidos, biografía y foto de perfil (opcional).</li>
          <li><strong>Datos de pago</strong>: procesados directamente por Stripe; nosotros solo almacenamos los identificadores de la transacción, importe y fecha.</li>
          <li><strong>Datos de uso</strong>: cursos en los que te inscribes, lecciones que completas, calificaciones obtenidas.</li>
          <li><strong>Datos técnicos</strong>: dirección IP y user-agent para sesiones y seguridad.</li>
          <li><strong>Token de echo (opcional)</strong>: si conectas tu cuenta echo, almacenamos el token cifrado con AES-256.</li>
        </ul>

        <h2>3. Para qué los usamos</h2>
        <ul>
          <li>Prestar el servicio (gestionar tu cuenta, mostrar tu progreso, procesar pagos).</li>
          <li>Comunicarnos contigo (verificación de email, recuperación de contraseña, avisos del servicio).</li>
          <li>Mejorar la plataforma (estadísticas agregadas y anónimas).</li>
        </ul>
        <p>No vendemos tus datos a terceros bajo ningún concepto.</p>

        <h2>4. Base legal</h2>
        <p>
          El tratamiento se basa en la <strong>ejecución del contrato</strong> entre tú y CourselyLabs (los Términos), en tu <strong>consentimiento</strong> para usos opcionales como el envío de novedades, y en nuestras <strong>obligaciones legales</strong> (facturación, seguridad).
        </p>

        <h2>5. Encargados de tratamiento</h2>
        <ul>
          <li><strong>Stripe</strong> — procesamiento de pagos.</li>
          <li><strong>MinIO</strong> u otro proveedor S3-compatible — almacenamiento de vídeos, PDFs y archivos subidos.</li>
          <li><strong>Servidor SMTP</strong> — envío transaccional de emails.</li>
        </ul>

        <h2>6. Tus derechos</h2>
        <p>
          Puedes ejercer en cualquier momento los derechos de acceso, rectificación, supresión, oposición, limitación y portabilidad escribiéndonos a <a href="mailto:privacidad@courselylabs.com">privacidad@courselylabs.com</a>. Tienes también derecho a presentar una reclamación ante la Agencia Española de Protección de Datos (<a href="https://www.aepd.es" target="_blank" rel="noopener">aepd.es</a>).
        </p>

        <h2>7. Conservación</h2>
        <p>
          Mantenemos tus datos mientras tengas cuenta activa. Si la eliminas, conservamos los datos mínimos necesarios para cumplir obligaciones legales (facturación, 6 años) y eliminamos el resto.
        </p>

        <h2>8. Cambios</h2>
        <p>
          Si modificamos sustancialmente esta política, te avisaremos por email antes de que entren en vigor.
        </p>
      </section>

      <div class="text-center q-mt-xl text-grey-7 text-caption">
        ¿Dudas? Escríbenos a <a href="mailto:privacidad@courselylabs.com">privacidad@courselylabs.com</a>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
const updatedAt = '14 de mayo de 2026'
</script>

<style scoped>
@import './_legal.scss';
</style>
