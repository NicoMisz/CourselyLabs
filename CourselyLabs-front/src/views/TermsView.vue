<template>
  <q-page class="q-pa-md q-pa-lg-lg legal-page">
    <div class="legal-wrap">
      <div class="text-center q-mb-lg">
        <q-icon name="gavel" size="42px" color="primary" />
        <h1 class="text-h4 q-mt-sm q-mb-xs page-title">Términos y condiciones</h1>
        <p class="text-body2 page-subtitle">Última actualización: {{ updatedAt }}</p>
      </div>

      <q-banner class="bg-orange-1 text-orange-10 q-mb-lg" rounded dense>
        <template v-slot:avatar>
          <q-icon name="gavel" color="orange-9" />
        </template>
        Texto orientativo. Sustitúyelo por unos términos definitivos redactados por tu asesor legal antes de operar comercialmente.
      </q-banner>

      <section class="prose">
        <h2>1. Aceptación</h2>
        <p>
          Al registrarte y usar CourselyLabs aceptas estos términos. Si no estás de acuerdo con ellos, no utilices el servicio.
        </p>

        <h2>2. Cuenta de usuario</h2>
        <p>
          Eres responsable de mantener la confidencialidad de tu cuenta y contraseña, y de todas las actividades que ocurran bajo tu cuenta. Debes proporcionar información veraz durante el registro y mantenerla actualizada.
        </p>

        <h2>3. Acceso a los cursos</h2>
        <p>
          Al inscribirte en un curso obtienes una <strong>licencia personal, intransferible y no exclusiva</strong> para acceder al contenido durante la vigencia de tu cuenta (o de tu suscripción Premium, según el caso). El contenido es para uso individual y no puede compartirse, redistribuirse ni revenderse.
        </p>

        <h2>4. Suscripción Premium</h2>
        <p>
          La suscripción Premium se factura de forma recurrente (mensual o anual). Puedes cancelarla en cualquier momento desde tu perfil; mantendrás el acceso Premium hasta el final del periodo ya pagado. No ofrecemos reembolsos automáticos; las solicitudes excepcionales se evalúan caso por caso escribiendo a soporte.
        </p>

        <h2>5. Pagos</h2>
        <p>
          Los pagos se procesan a través de <strong>Stripe</strong>. CourselyLabs no almacena los datos de tu tarjeta. Eres responsable de mantener vigentes los métodos de pago que asocies a tu cuenta.
        </p>

        <h2>6. Propiedad intelectual</h2>
        <p>
          Todo el contenido publicado por instructores y por CourselyLabs (vídeos, textos, PDFs, código, ejercicios, evaluaciones) está protegido por derechos de autor. Queda prohibida su reproducción, distribución o comunicación pública sin autorización expresa.
        </p>

        <h2>7. Contenido subido por usuarios</h2>
        <p>
          Si eres instructor, garantizas que el contenido que subes es de tu propiedad o cuentas con licencia para distribuirlo. Concedes a CourselyLabs una licencia no exclusiva para alojarlo, mostrarlo y distribuirlo a los alumnos.
        </p>
        <p>
          Si eres alumno, las entregas (proyectos, respuestas) las conserva tu instructor y CourselyLabs solo a efectos de calificación y trazabilidad del progreso.
        </p>

        <h2>8. Laboratorios virtuales</h2>
        <p>
          Los entornos virtuales (laboratorios) se proporcionan «tal cual» para fines educativos. Está prohibido usarlos para minería de criptomonedas, ataques a terceros, almacenamiento de contenido ilegal o cualquier actividad que viole la legislación aplicable. CourselyLabs puede suspender el acceso a un alumno que incumpla esto.
        </p>

        <h2>9. Conducta</h2>
        <p>
          Está prohibido compartir contenido ofensivo, hacer un uso fraudulento de la plataforma, intentar acceder a cuentas ajenas o eludir las medidas de seguridad. Las infracciones graves implican la suspensión de la cuenta.
        </p>

        <h2>10. Disponibilidad</h2>
        <p>
          Hacemos todo lo razonable para mantener el servicio disponible, pero no garantizamos un nivel de disponibilidad determinado ni respondemos por interrupciones imputables a proveedores de infraestructura, fuerza mayor o mantenimiento programado.
        </p>

        <h2>11. Modificaciones</h2>
        <p>
          Podemos modificar estos términos cuando sea necesario. Si los cambios son sustanciales te avisaremos por email antes de que entren en vigor.
        </p>

        <h2>12. Ley aplicable</h2>
        <p>
          Estos términos se rigen por la legislación española. Cualquier disputa se someterá a los juzgados que correspondan según la normativa de consumo aplicable.
        </p>

        <h2>13. Contacto</h2>
        <p>
          Para cualquier consulta: <a href="mailto:legal@courselylabs.com">legal@courselylabs.com</a>.
        </p>
      </section>

      <div class="text-center q-mt-xl">
        <p class="text-body2 text-grey-7">
          Consulta también nuestra
          <router-link to="/privacidad">política de privacidad</router-link>
          y
          <router-link to="/cookies">política de cookies</router-link>.
        </p>
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
