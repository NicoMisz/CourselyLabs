<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import StarRating from './StarRating.vue'

interface Props {
  loading?: boolean
  initialRating?: number
  initialComment?: string
  submitLabel?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  initialRating: 0,
  initialComment: '',
  submitLabel: 'Enviar'
})

const emit = defineEmits<{
  submit: [{ rating: number; comment: string }]
  cancel: []
}>()

// Estado interno
const rating = ref(props.initialRating)
const comment = ref(props.initialComment)

// Validaciones
const isRatingValid = computed(() => rating.value >= 1 && rating.value <= 5)
const isCommentValid = computed(() => comment.value.trim().length >= 20)

const isValid = computed(() => isRatingValid.value && isCommentValid.value)

// Reset si cambian props (útil en edición inline)
watch(
  () => props.initialRating,
  v => (rating.value = v)
)

watch(
  () => props.initialComment,
  v => (comment.value = v)
)

function submitForm() {
  if (!isValid.value) return
  emit('submit', {
    rating: rating.value,
    comment: comment.value.trim()
  })
}
</script>

<template>
  <div class="review-form">

    <!-- Rating -->
    <div class="field">
      <label class="label">Tu valoración</label>
      <StarRating v-model="rating" size="lg" />
      <div v-if="!isRatingValid" class="error">
        Selecciona una puntuación entre 1 y 5.
      </div>
    </div>

    <!-- Comentario -->
    <div class="field">
      <label class="label">Comentario</label>
      <q-input
        v-model="comment"
        type="textarea"
        autogrow
        outlined
        counter
        :maxlength="2000"
        placeholder="Comparte tu experiencia con el curso..."
      />
      <div v-if="!isCommentValid" class="error">
        El comentario debe tener al menos 20 caracteres.
      </div>
    </div>

    <!-- Acciones -->
    <div class="actions">
      <q-btn
        color="primary"
        :disable="!isValid || loading"
        :loading="loading"
        :label="submitLabel"
        @click="submitForm"
      />

      <q-btn
        flat
        color="grey-7"
        label="Cancelar"
        @click="emit('cancel')"
      />
    </div>

  </div>
</template>

<style scoped>
.review-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-weight: 600;
}

.error {
  color: #d32f2f;
  font-size: 0.85rem;
}

.actions {
  display: flex;
  gap: 12px;
}
</style>
