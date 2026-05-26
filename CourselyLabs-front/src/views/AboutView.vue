<template>
  <q-page class="q-pa-md q-pa-lg-lg legal-page">
    <div class="legal-wrap">
      <div class="text-center q-mb-lg">
        <q-icon name="info" size="42px" color="primary" />
        <h1 class="text-h4 q-mt-sm q-mb-xs page-title">Sobre CourselyLabs</h1>
        <p class="text-body2 page-subtitle">Una plataforma de cursos pensada para aprender practicando.</p>
      </div>

      <section class="prose">
        <h2>Qué es CourselyLabs</h2>
        <p>
          CourselyLabs es una plataforma de cursos online donde cualquier persona puede aprender o enseñar. Nuestro punto diferenciador son los <strong>laboratorios virtuales integrados</strong>: cuando estudias programación, redes o sistemas, no te limitas a leer y mirar vídeos — abres un entorno Linux real desde la propia lección y practicas en serio.
        </p>

        <h2>Cómo está construido</h2>
        <ul>
          <li><strong>Editor de cursos por bloques</strong>: texto, vídeo, PDF, cuestionario, proyecto, pregunta abierta y laboratorio. El instructor combina libremente.</li>
          <li><strong>Tres tipos de evaluación</strong>: cuestionarios autocorregidos, entregas de archivo y preguntas abiertas calificadas a mano.</li>
          <li><strong>Rutas de aprendizaje con prerequisitos</strong>: configurables curso a curso con umbral mínimo.</li>
          <li><strong>Suscripción Premium única</strong>: no compras curso a curso; un solo plan desbloquea todo el contenido Premium.</li>
        </ul>

        <h2>Nuestro stack</h2>
        <p>
          Vue 3 + Quasar + TypeScript para el frontend, Spring Boot 4 + Java 21 + PostgreSQL 16 para el backend, MinIO para almacenamiento de vídeos y PDFs, Stripe para suscripciones, e integración con echo lab sobre Proxmox para las máquinas virtuales.
        </p>

        <h2>El equipo</h2>
        <p>
          Somos un equipo pequeño que cree en la formación práctica de verdad. Si quieres ponerte en contacto con nosotros, escríbenos a <a href="mailto:hola@courselylabs.com">hola@courselylabs.com</a> o desde la <router-link to="/contacto">página de contacto</router-link>.
        </p>

        <h2>¿Eres instructor?</h2>
        <p>
          Cualquier usuario verificado puede crear cursos. <router-link to="/instructor">Empieza aquí</router-link> y publica tu primer curso en minutos.
        </p>
      </section>
    </div>
  </q-page>
</template>

<script setup lang="ts">
// vista informativa estática
</script>

<style scoped>
@import './_legal.scss';
</style>
