import api from './axios'

export interface LessonPayload {
  title: string
  description?: string
  type?: string
  contentText?: string
  contentUrl?: string
  duration?: number
  position?: number
  isFree?: boolean
}

export async function createLesson(sectionId: string, payload: LessonPayload) {
  const { data } = await api.post(`/api/sections/${sectionId}/lessons`, payload)
  return data
}

export async function updateLesson(id: string, payload: LessonPayload) {
  const { data } = await api.put(`/api/lessons/${id}`, payload)
  return data
}

export async function deleteLesson(id: string) {
  await api.delete(`/api/lessons/${id}`)
}

export async function reorderLessons(items: { id: string; position: number }[]) {
  await api.patch('/api/lessons/reorder', { items })
}
