<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useQuasar } from 'quasar'

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
interface Review {
  id: string
  rating: 1 | 2 | 3 | 4 | 5
  comment: string
  createdAt: string
  userName: string
  userAvatar?: string | null
  isOwn?: boolean
}

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

  calculateDistribution()
})

// -----------------------------
// API simulada (reemplazar por real)
// -----------------------------
async function loadSummary() {
  loadingSummary.value = true
  try {
    const res = await fakeApiSummary()
    average.value = res.average
    total.value = res.total
    distribution.value = res.distribution
  } finally {
    loadingSummary.value = false
  }
}

async function loadReviews(pageNumber: number) {
  loadingList.value = true
  try {
    const res = await fakeApiReviews()
    if (pageNumber === 0) {
      reviews.value = res.items
    } else {
      reviews.value = [...reviews.value, ...res.items]
    }
    hasMore.value = res.hasMore
    page.value = pageNumber
  } finally {
    loadingList.value = false
  }
}

async function loadMyReview() {
  try {
    myReview.value = await fakeApiMyReview()
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
    dist[r.rating]++
  })

  distribution.value = dist
  total.value = reviews.value.length
  average.value =
    reviews.value.reduce((acc, r) => acc + r.rating, 0) /
    (reviews.value.length || 1)
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

// -----------------------------
// CRUD
// -----------------------------
// eslint-disable-next-line @typescript-eslint/no-unused-vars
async function submitReview(_payload: { rating: number; comment: string }) {
  // no usamos payload en las fake APIs
  loadingAction.value = true
  try {
    if (editingReviewId.value) {
      await fakeApiUpdate()
      $q.notify({ type: 'positive', message: 'Valoración actualizada' })
    } else {
      await fakeApiCreate()
      $q.notify({ type: 'positive', message: 'Valoración enviada' })
    }

    editingReviewId.value = null
    await reloadAll()
  } finally {
    loadingAction.value = false
  }
}

async function deleteReview(id: string) {
  void id // evita el warning
  loadingAction.value = true
  try {
    await fakeApiDelete()
    $q.notify({ type: 'positive', message: 'Valoración eliminada' })
    await reloadAll()
  } finally {
    loadingAction.value = false
  }
}

async function reloadAll() {
  await loadReviews(0)
  await loadMyReview()
  calculateDistribution()
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

// -----------------------------
// Fake API (solo ejemplo)
// -----------------------------
async function fakeApiSummary(): Promise<{
  average: number
  total: number
  distribution: Record<1 | 2 | 3 | 4 | 5, number>
}> {
  return {
    average: 4.3,
    total: 128,
    distribution: { 5: 80, 4: 30, 3: 10, 2: 5, 1: 3 }
  }
}

async function fakeApiReviews(): Promise<{
  items: Review[]
  hasMore: boolean
}> {
  return {
    items: [
      {
        id: '1',
        userName: 'Ana López',
        rating: 5,
        comment: 'Excelente curso!',
        createdAt: '2024-01-10',
        isOwn: false
      }
    ],
    hasMore: false
  }
}

async function fakeApiMyReview(): Promise<Review | null> {
  return null
}

async function fakeApiCreate(): Promise<void> {}
async function fakeApiUpdate(): Promise<void> {}
async function fakeApiDelete(): Promise<void> {}
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
    </div>

    <div class="q-mt-xl">
      <ReviewList
        :reviews="reviews"
        :has-more="hasMore"
        :loading-more="loadingList"
        @load-more="loadReviews(page + 1)"
        @edit="startEdit"
        @delete="deleteReview"
      />
    </div>
  </div>
</template>
