import api from './axios'
import type { LessonProgress, CourseProgress } from '../types/progress'

export async function toggleLessonComplete(lessonId: string): Promise<LessonProgress> {
  const { data } = await api.post<LessonProgress>(`/api/progress/lessons/${lessonId}/complete`)
  return data
}

export async function getCourseProgress(courseId: string): Promise<CourseProgress> {
  const { data } = await api.get<CourseProgress>(`/api/progress/courses/${courseId}`)
  return data
}

export async function getLessonProgress(lessonId: string): Promise<LessonProgress> {
  const { data } = await api.get<LessonProgress>(`/api/progress/lessons/${lessonId}`)
  return data
}

export async function updateLessonPosition(lessonId: string, positionSeconds: number): Promise<LessonProgress> {
  const { data } = await api.patch<LessonProgress>(`/api/progress/lessons/${lessonId}/position`, {
    positionSeconds,
  })
  return data
}
