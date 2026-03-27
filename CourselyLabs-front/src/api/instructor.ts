import api from './axios'
import type { CourseDetail } from '../types/course'

export interface CreateCoursePayload {
  title: string
  slug: string
  description: string
  shortDescription?: string
  thumbnailUrl?: string
  categoryId?: number
  level?: string
  isFree?: boolean
  price?: number
}

export interface CourseLimits {
  currentCourses: number
  maxCourses: number
}

export async function getMyCreatedCourses() {
  const { data } = await api.get('/api/courses/mine')
  return data
}

export async function getMyCourseLimits(): Promise<CourseLimits> {
  const { data } = await api.get<CourseLimits>('/api/courses/mine/limits')
  return data
}

export async function createCourse(payload: CreateCoursePayload) {
  const { data } = await api.post('/api/courses', payload)
  return data
}

export async function updateCourse(id: string, payload: Partial<CreateCoursePayload>) {
  const { data } = await api.put(`/api/courses/${id}`, payload)
  return data
}

export async function deleteCourse(id: string) {
  await api.delete(`/api/courses/${id}`)
}

export async function submitForReview(id: string) {
  const { data } = await api.patch(`/api/courses/${id}/submit-review`)
  return data
}

export async function publishCourse(id: string) {
  const { data } = await api.patch(`/api/courses/${id}/publish`)
  return data
}

export async function unpublishCourse(id: string) {
  const { data } = await api.patch(`/api/courses/${id}/unpublish`)
  return data
}

export async function getCourseForEdit(id: string): Promise<CourseDetail> {
  const { data } = await api.get<CourseDetail>(`/api/courses/${id}`)
  return data
}
