import api from './axios'
import type { LessonBlock, BlockType } from '../types/lesson'

export async function listBlocks(lessonId: string): Promise<LessonBlock[]> {
  const { data } = await api.get<LessonBlock[]>(`/api/lessons/${lessonId}/blocks`)
  return data
}

export async function createBlock(
  lessonId: string,
  payload: { type: BlockType; position?: number; textContent?: string; videoUrl?: string; pdfUrl?: string },
): Promise<LessonBlock> {
  const { data } = await api.post<LessonBlock>(`/api/lessons/${lessonId}/blocks`, payload)
  return data
}

export async function updateBlock(blockId: string, payload: Partial<LessonBlock>): Promise<LessonBlock> {
  const { data } = await api.put<LessonBlock>(`/api/blocks/${blockId}`, payload)
  return data
}

export async function deleteBlock(blockId: string): Promise<void> {
  await api.delete(`/api/blocks/${blockId}`)
}

export async function reorderBlocks(lessonId: string, orderedIds: string[]): Promise<void> {
  await api.patch(`/api/lessons/${lessonId}/blocks/reorder`, { orderedIds })
}

export function isAssessmentBlockType(type: string): boolean {
  return type === 'quiz' || type === 'project' || type === 'open_text'
}
