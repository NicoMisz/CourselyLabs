export interface Review {
  id: string
  courseId: string
  courseTitle?: string
  userId: string
  userFullName?: string
  rating: number
  comment: string
  createdAt?: string
  updatedAt?: string
}

export interface ReviewPage {
  content: Review[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export type ReviewSort = 'recent' | 'best' | 'worst'

export interface CreateReviewPayload {
  rating: number
  comment: string
}

export interface UpdateReviewPayload {
  rating: number
  comment: string
}