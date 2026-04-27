<template>
  <q-card flat bordered class="submit-card">
    <q-card-section>
      <div class="text-h6 q-mb-sm">
        <q-icon name="edit_note" color="deep-purple" /> Respuesta abierta
      </div>
      <p class="text-body2 text-grey-7">
        Escribe tu respuesta. El instructor la revisara y te dara feedback.
      </p>
    </q-card-section>

    <q-card-section>
      <q-input
        v-model="answerText"
        type="textarea"
        outlined
        rows="14"
        placeholder="Escribe aqui tu respuesta..."
        counter
        maxlength="20000"
      />
    </q-card-section>

    <q-card-actions align="right">
      <q-btn
        color="primary"
        unelevated
        no-caps
        icon="send"
        label="Enviar respuesta"
        :loading="submitting"
        :disable="answerText.trim().length < 10"
        @click="handleSubmit"
      />
    </q-card-actions>
  </q-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useQuasar } from 'quasar'
import { submitOpenText } from '../api/assessment'

const props = defineProps<{ attemptId: string }>()
const emit = defineEmits<{ submitted: [] }>()

const $q = useQuasar()
const answerText = ref('')
const submitting = ref(false)

async function handleSubmit() {
  submitting.value = true
  try {
    await submitOpenText(props.attemptId, answerText.value)
    emit('submitted')
  } catch (err: any) {
    $q.notify({
      type: 'negative',
      message: err?.response?.data?.message || 'Error al enviar',
      position: 'bottom-right',
    })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.submit-card {
  border-radius: 12px;
  max-width: 800px;
  margin: 0 auto;
}
</style>
