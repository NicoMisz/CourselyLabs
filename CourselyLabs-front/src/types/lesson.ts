export type LessonType = 'video' | 'text' | 'pdf' | 'audio'

export interface Lesson {
  id: string
  title: string
  description?: string
  type: LessonType
  contentUrl?: string
  contentText?: string
  duration?: number
  position: number
  isFree: boolean
  sectionId: string
  createdAt?: string
  updatedAt?: string
}

export interface Section {
  id: string
  title: string
  description?: string
  position: number
  courseId: string
  lessons: Lesson[]
  createdAt?: string
  updatedAt?: string
}
