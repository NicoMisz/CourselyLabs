import api from './axios'
/* import type {
  Review,
  ReviewPage,
  CreateReviewPayload,
  UpdateReviewPayload,
} from '../types/review' */

export async function getCourseReviewsPaged(
  courseId: string,
  page = 0,
  size = 10,
  sort = 'createdAt,desc',
) {
  const { data } = await api.get(`/api/reviews/course/${courseId}/paged`, {
    params: { page, size, sort },
  })
  return data
}

export async function getCourseAverage(courseId: string) {
  const { data } = await api.get(`/api/reviews/course/${courseId}/average`)
  return data ?? 0
}

export async function getMyReview(courseId: string) {
  try {
    const { data } = await api.get(`/api/reviews/course/${courseId}/me`)
    return data
  } catch (error: unknown) {
    const status = (error as { response?: { status?: number } })?.response?.status
    if (status === 404) return null
    throw error
  }
}

export async function createReview(courseId: string, payload: { rating: number; comment: string }) {
  const { data } = await api.post(`/api/reviews/course/${courseId}`, payload)
  return data
}

// IMPORTANTE: usar endpoints OWN
export async function updateReview(reviewId: string, payload: { rating: number; comment: string }) {
  const { data } = await api.put(`/api/reviews/${reviewId}/own`, payload)
  return data
}

export async function deleteReview(reviewId: string) {
  await api.delete(`/api/reviews/${reviewId}/own`)
}