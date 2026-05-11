export type LessonType = 'video' | 'text' | 'pdf' | 'audio'

export type BlockType = 'text' | 'video' | 'pdf' | 'quiz' | 'project' | 'open_text' | 'lab'

export interface LessonBlock {
  id: string
  lessonId: string
  type: BlockType
  position: number
  textContent?: string
  videoUrl?: string
  pdfUrl?: string
  /** Populated for quiz/project/open_text blocks once the assessment exists. */
  assessmentId?: string
  /** Populated for lab blocks. */
  labProvider?: string
  labTemplateId?: number
  labInstructions?: string
}

export interface Lesson {
  id: string
  title: string
  description?: string
  /** Legacy single-type — content lives in {@link blocks} now. */
  type?: LessonType | string
  contentUrl?: string
  contentText?: string
  duration?: number
  position: number
  isFree: boolean
  sectionId: string
  createdAt?: string
  updatedAt?: string
  blocks?: LessonBlock[]
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
