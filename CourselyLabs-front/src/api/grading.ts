import api from './axios'

export interface PendingSubmission {
  id: string
  attemptId: string
  type: 'project' | 'open_text'
  fileName?: string
  fileSize?: number
  answerText?: string
  createdAt: string

  studentId: string
  studentName: string

  courseId: string
  courseTitle: string

  lessonId: string
  lessonTitle: string

  blockId?: string
  assessmentId: string
  assessmentType: 'project' | 'open_text'
}

export async function listPendingSubmissions(): Promise<PendingSubmission[]> {
  const { data } = await api.get<PendingSubmission[]>('/api/grading/pending')
  return data
}
