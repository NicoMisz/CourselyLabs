import api from './axios'
import type {
  Review,
  ReviewPage,
  CreateReviewPayload,
  UpdateReviewPayload,
} from '../types/review'

export async function getCourseReviewsPaged(
  courseId: string,
  page = 0,
  size = 10,
  sort = 'createdAt,desc',
): Promise<ReviewPage> {
  const { data } = await api.get<ReviewPage>(`/api/reviews/course/${courseId}/paged`, {
    params: { page, size, sort },
  })
  return data
}

export async function getCourseAverage(courseId: string): Promise<number> {
  const { data } = await api.get<number>(`/api/reviews/course/${courseId}/average`)
  return data ?? 0
}

export async function getMyReview(courseId: string): Promise<Review | null> {
  try {
    const { data } = await api.get<Review>(`/api/reviews/course/${courseId}/me`)
    return data
  } catch (error: unknown) {
    const status = (error as { response?: { status?: number } })?.response?.status
    if (status === 404) return null
    throw error
  }
}

export async function createReview(courseId: string, payload: CreateReviewPayload): Promise<Review> {
  const { data } = await api.post<Review>(`/api/reviews/course/${courseId}`, payload)
  return data
}

export async function updateReview(reviewId: string, payload: UpdateReviewPayload): Promise<Review> {
  const { data } = await api.put<Review>(`/api/reviews/${reviewId}`, payload)
  return data
}

export async function deleteReview(reviewId: string): Promise<void> {
  await api.delete(`/api/reviews/${reviewId}`)
}