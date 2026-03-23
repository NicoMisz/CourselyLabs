export interface LessonProgress {
  id?: string
  lessonId: string
  isCompleted: boolean
  completedAt?: string
  lastPositionSeconds: number
}

export interface CourseProgress {
  courseId: string
  totalLessons: number
  completedLessons: number
  progressPercent: number
  completedLessonIds: string[]
}
