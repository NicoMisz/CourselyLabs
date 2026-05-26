<template>
  <q-page class="q-pa-md q-pa-lg-lg legal-page">
    <div class="legal-wrap">
      <div class="text-center q-mb-lg">
        <q-icon name="mail" size="42px" color="primary" />
        <h1 class="text-h4 q-mt-sm q-mb-xs page-title">Contacto</h1>
        <p class="text-body2 page-subtitle">¿Necesitas ayuda? Escríbenos.</p>
      </div>

      <div class="row q-col-gutter-xl">
        <!-- Canales directos -->
        <div class="col-12 col-md-5">
          <h2 class="text-h6 cat-title q-mt-none">Canales directos</h2>

          <q-list bordered separator>
            <q-item>
              <q-item-section avatar><q-icon name="support_agent" color="primary" /></q-item-section>
              <q-item-section>
                <q-item-label>Soporte general</q-item-label>
                <q-item-label caption>
                  <a href="mailto:soporte@courselylabs.com">soporte@courselylabs.com</a>
                </q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section avatar><q-icon name="privacy_tip" color="primary" /></q-item-section>
              <q-item-section>
                <q-item-label>Privacidad y datos</q-item-label>
                <q-item-label caption>
                  <a href="mailto:privacidad@courselylabs.com">privacidad@courselylabs.com</a>
                </q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section avatar><q-icon name="business" color="primary" /></q-item-section>
              <q-item-section>
                <q-item-label>Empresas y formación</q-item-label>
                <q-item-label caption>
                  <a href="mailto:b2b@courselylabs.com">b2b@courselylabs.com</a>
                </q-item-label>
              </q-item-section>
            </q-item>
          </q-list>

          <p class="text-caption text-grey-7 q-mt-md">
            Respondemos en horario de oficina (CET), de lunes a viernes. Si tu suscripción es Premium, soporte prioritario.
          </p>
        </div>

        <!-- Formulario -->
        <div class="col-12 col-md-7">
          <h2 class="text-h6 cat-title q-mt-none">O envíanos un mensaje</h2>

          <q-form @submit.prevent="onSubmit" class="q-gutter-md">
            <q-input
              v-model="form.name"
              outlined
              label="Tu nombre"
              :rules="[v => !!v || 'Obligatorio']"
            />
            <q-input
              v-model="form.email"
              outlined
              type="email"
              label="Tu email"
              :rules="[
                v => !!v || 'Obligatorio',
                v => /.+@.+\..+/.test(v) || 'Email no válido',
              ]"
            />
            <q-select
              v-model="form.topic"
              outlined
              :options="topics"
              label="Asunto"
              emit-value
              map-options
            />
            <q-input
              v-model="form.message"
              outlined
              type="textarea"
              rows="5"
              label="Mensaje"
              :rules="[v => (v && v.length >= 10) || 'Cuéntanos un poco más (10 caracteres mínimo)']"
            />

            <div class="row justify-end">
              <q-btn
                type="submit"
                unelevated
                color="primary"
                label="Enviar mensaje"
                icon-right="send"
                no-caps
                :loading="sending"
              />
            </div>
          </q-form>

          <q-banner v-if="sent" class="bg-positive text-white q-mt-md" rounded>
            <template v-slot:avatar>
              <q-icon name="check_circle" color="white" />
            </template>
            ¡Gracias! Hemos recibido tu mensaje y te responderemos pronto.
          </q-banner>
        </div>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useQuasar } from 'quasar'

const $q = useQuasar()
const sending = ref(false)
const sent = ref(false)

const form = reactive({
  name: '',
  email: '',
  topic: 'general',
  message: '',
})

const topics = [
  { label: 'Consulta general', value: 'general' },
  { label: 'Problema técnico', value: 'tecnico' },
  { label: 'Facturación / Premium', value: 'facturacion' },
  { label: 'Empresas / B2B', value: 'b2b' },
  { label: 'Otro', value: 'otro' },
]

async function onSubmit() {
  sending.value = true
  // Por ahora el backend no expone endpoint de contacto: simulamos envío.
  await new Promise(r => setTimeout(r, 600))
  sent.value = true
  sending.value = false
  $q.notify({ type: 'positive', message: 'Mensaje enviado', position: 'bottom-right' })
}
</script>

<style scoped>
@import './_legal.scss';
.cat-title {
  font-family: 'Monda', sans-serif;
  color: var(--app-text-strong);
}
</style>
