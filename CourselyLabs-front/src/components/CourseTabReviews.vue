<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import type { Review } from '../types/review'

import {
  getCourseReviewsPaged,
  getCourseAverage,
  getMyReview,
  createReview,
  updateReview,
  deleteReview
} from '../api/review'

import RatingDistribution from './RatingDistribution.vue'
import ReviewList from './ReviewList.vue'
import ReviewForm from './ReviewForm.vue'

interface Props {
  courseId: string
  enrolled: boolean
  isLoggedIn: boolean
  completedLessons: number
  currentUserId?: string
}

const props = defineProps<Props>()
const $q = useQuasar()

// -----------------------------
// Tipos
// -----------------------------

// -----------------------------
// Estado
// -----------------------------
const loadingSummary = ref(true)
const loadingList = ref(true)
const loadingAction = ref(false)

const average = ref(0)
const total = ref(0)

const distribution = ref<Record<1 | 2 | 3 | 4 | 5, number>>({
  1: 0,
  2: 0,
  3: 0,
  4: 0,
  5: 0
})

const myReview = ref<Review | null>(null)
const reviews = ref<Review[]>([])
const page = ref(0)
const hasMore = ref(false)

const editingReviewId = ref<string | null>(null)

// -----------------------------
// Carga inicial
// -----------------------------
onMounted(async () => {
  await loadSummary()
  await loadReviews(0)

  if (props.isLoggedIn) {
    await loadMyReview()
  }

  //calculateDistribution()
})

// -----------------------------
// API real
// -----------------------------
async function loadSummary() {
  loadingSummary.value = true
  try {
    average.value = await getCourseAverage (props.courseId)
  } catch{
    $q.notify({ type: 'negative', message: 'Error al cargar la valoración media'})
  }finally {
    loadingSummary.value = false
  }
}

async function loadReviews(pageNumber: number) {
  loadingList.value = true
  try {
    const data = await getCourseReviewsPaged(props.courseId, pageNumber)
    if (pageNumber === 0) {
      reviews.value = data.content
    } else {
      reviews.value = [...reviews.value, ...data.content]
    }
    hasMore.value = !data.last
    total.value = data.totalElements
    page.value = pageNumber
    calculateDistribution()
  } catch{
    $q.notify({ type: 'negative', message: 'Error al cargar las reseñas' })
  } finally {
    loadingList.value = false
  }
}

async function loadMyReview() {
  try {
    myReview.value = await getMyReview(props.courseId)
  } catch {
    myReview.value = null
  }
}

// -----------------------------
// Distribución local
// -----------------------------
function calculateDistribution() {
  const dist: Record<1 | 2 | 3 | 4 | 5, number> = {
    1: 0,
    2: 0,
    3: 0,
    4: 0,
    5: 0
  }

  reviews.value.forEach((r: Review) => {
    dist[r.rating as 1 | 2 | 3 | 4 | 5]++
  })

  distribution.value = dist
}

// -----------------------------
// Reglas de visibilidad
// -----------------------------
const canReview = computed(() =>
  props.isLoggedIn &&
  props.enrolled &&
  props.completedLessons > 0 &&
  !myReview.value
)

// Mensaje guía cuando el usuario no puede dejar review aún.
// Solo aplica si todavía no ha valorado (myReview === null).
const reviewBlockedReason = computed<string | null>(() => {
  if (myReview.value) return null
  if (!props.isLoggedIn) return 'Inicia sesión e inscríbete al curso para poder valorarlo.'
  if (!props.enrolled) return 'Inscríbete al curso y completa al menos una lección para poder valorarlo.'
  if (props.completedLessons === 0) return 'Completa al menos una lección para poder valorar el curso.'
  return null
})

// -----------------------------
// CRUD
// -----------------------------

async function submitReview(payload: { rating: number; comment: string }) {
  loadingAction.value = true
  try {
    if (editingReviewId.value) {
      await updateReview(editingReviewId.value, payload)
      $q.notify({ type: 'positive', message: 'Valoración actualizada' })
    } else {
      await createReview(props.courseId, payload)
      $q.notify({ type: 'positive', message: 'Valoración enviada' })
    }
    editingReviewId.value = null
    await reloadAll()
  } finally {
    loadingAction.value = false
  }
}

async function handleDeleteReview(id: string) {
  loadingAction.value = true
  try {
    await deleteReview(id)
    $q.notify({ type: 'positive', message: 'Valoración eliminada' })
    await reloadAll()
  } catch {
    $q.notify({ type: 'negative', message: 'Error al eliminar la valoración' })
  } finally {
    loadingAction.value = false
  }
}

async function reloadAll() {
  await loadSummary()
  await loadReviews(0)
  await loadMyReview()
}

// -----------------------------
// Acciones UI
// -----------------------------
function startEdit(id: string) {
  editingReviewId.value = id
  const r = reviews.value.find(r => r.id === id)
  if (r) {
    myReview.value = r
  }
}

function cancelEdit() {
  editingReviewId.value = null
  loadMyReview()
}

</script>

<template>
  <div class="q-pa-md">
    <RatingDistribution
      v-if="!loadingSummary"
      :average="average"
      :total="total"
      :distribution="distribution"
    />

    <q-skeleton v-else type="rect" height="120px" />

    <div class="q-mt-lg">
      <ReviewForm
        v-if="canReview || editingReviewId"
        :initial-rating="myReview?.rating"
        :initial-comment="myReview?.comment"
        :loading="loadingAction"
        :submit-label="editingReviewId ? 'Guardar cambios' : 'Enviar valoración'"
        @submit="submitReview"
        @cancel="cancelEdit"
      />

      <q-banner v-else-if="reviewBlockedReason" class="app-banner-soft" rounded>
        <template v-slot:avatar>
          <q-icon name="info" color="primary" />
        </template>
        {{ reviewBlockedReason }}
      </q-banner>
    </div>

    <div class="q-mt-xl">
      <ReviewList
        :reviews="reviews"
        :has-more="hasMore"
        :loading-more="loadingList"
        @load-more="loadReviews(page + 1)"
        @edit="startEdit"
        @delete="handleDeleteReview"
      />
    </div>
  </div>
</template>
