import api from './axios'

export interface SectionPayload {
  title: string
  description?: string
  position?: number
}

export async function createSection(courseId: string, payload: SectionPayload) {
  const { data } = await api.post(`/api/courses/${courseId}/sections`, payload)
  return data
}

export async function updateSection(id: string, payload: SectionPayload) {
  const { data } = await api.put(`/api/sections/${id}`, payload)
  return data
}

export async function deleteSection(id: string) {
  await api.delete(`/api/sections/${id}`)
}

export async function reorderSections(items: { id: string; position: number }[]) {
  await api.patch('/api/sections/reorder', { items })
}
