export type AssessmentType = 'quiz' | 'project' | 'open_text'
export type AttemptStatus = 'in_progress' | 'submitted' | 'graded' | 'expired'

export interface QuizOption {
  id: string
  optionText: string
  isCorrect?: boolean
  explanation?: string
  position?: number
}

export interface QuizQuestion {
  id: string
  questionText: string
  position?: number
  points?: number
  options?: QuizOption[]
}

export interface Assessment {
  id: string
  lessonId: string
  type: AssessmentType
  description?: string
  maxAttempts: number
  timeLimitMinutes?: number | null
  passingScore: number
  shuffleOptions: boolean
  questions?: QuizQuestion[]
}

export interface AssessmentAttempt {
  id: string
  assessmentId: string
  startedAt: string
  submittedAt?: string | null
  score?: number | null
  passed?: boolean | null
  attemptNumber: number
  status: AttemptStatus
}

export interface QuizAnswer {
  questionId: string
  selectedOptionId?: string | null
}

export interface AssessmentResult {
  attemptId: string
  score?: number | null
  maxScore?: number | null
  passed?: boolean | null
  passingScore: number
  questions: QuizQuestion[]
  answers: QuizAnswer[]
}

export interface Submission {
  id: string
  attemptId: string
  type: 'project' | 'open_text'
  fileName?: string
  fileSize?: number
  answerText?: string
  instructorFeedback?: string
  gradedAt?: string | null
  gradedById?: string | null
  createdAt: string
  studentName?: string
  studentId?: string
}

export interface StartedAttempt {
  attemptId: string
  assessment: Assessment
}
