<template>
  <q-page class="flex flex-center">
    <div class="text-center q-pa-xl" style="max-width: 500px">
      <q-icon name="info" size="64px" color="grey-5" class="q-mb-lg" />
      <h1 class="text-h5 q-mb-sm">Pago cancelado</h1>
      <p class="text-body1 text-grey-7 q-mb-lg">
        No se ha realizado ningún cobro. Puedes volver a intentarlo cuando quieras.
      </p>
      <div class="row q-gutter-md justify-center">
        <q-btn color="primary" label="Ver planes" to="/premium" unelevated no-caps />
        <q-btn outline color="primary" label="Volver al inicio" to="/" no-caps />
      </div>
    </div>
  </q-page>
</template>
