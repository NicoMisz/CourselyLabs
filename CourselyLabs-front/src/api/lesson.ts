import api from './axios'
import type { Section, Lesson } from '../types/lesson'

export async function getCourseSections(courseId: string): Promise<Section[]> {
  const { data } = await api.get<Section[]>(`/api/courses/${courseId}/sections`)
  return data
}

export async function getLessonById(lessonId: string): Promise<Lesson> {
  const { data } = await api.get<Lesson>(`/api/lessons/${lessonId}`)
  return data
}
