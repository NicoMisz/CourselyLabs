import api from './axios'
import type {
  Assessment,
  AssessmentAttempt,
  AssessmentResult,
  QuizAnswer,
  QuizQuestion,
  StartedAttempt,
  Submission,
} from '../types/assessment'

// --- Read ---

export async function getAssessmentByLesson(lessonId: string): Promise<Assessment | null> {
  try {
    const { data } = await api.get<Assessment>(`/api/lessons/${lessonId}/assessment`)
    return data
  } catch (e: any) {
    if (e?.response?.status === 204) return null
    return null
  }
}

export async function getAssessmentForEdit(lessonId: string): Promise<Assessment | null> {
  try {
    const { data } = await api.get<Assessment>(`/api/lessons/${lessonId}/assessment/edit`)
    return data
  } catch {
    return null
  }
}

// --- CRUD (instructor) ---

export async function createAssessment(lessonId: string, dto: Partial<Assessment>): Promise<Assessment> {
  const { data } = await api.post<Assessment>(`/api/lessons/${lessonId}/assessment`, dto)
  return data
}

export async function updateAssessment(id: string, dto: Partial<Assessment>): Promise<Assessment> {
  const { data } = await api.put<Assessment>(`/api/assessments/${id}`, dto)
  return data
}

export async function deleteAssessment(id: string): Promise<void> {
  await api.delete(`/api/assessments/${id}`)
}

export async function addQuestion(assessmentId: string, q: Partial<QuizQuestion>): Promise<QuizQuestion> {
  const { data } = await api.post<QuizQuestion>(`/api/assessments/${assessmentId}/questions`, q)
  return data
}

export async function updateQuestion(questionId: string, q: Partial<QuizQuestion>): Promise<QuizQuestion> {
  const { data } = await api.put<QuizQuestion>(`/api/questions/${questionId}`, q)
  return data
}

export async function deleteQuestion(questionId: string): Promise<void> {
  await api.delete(`/api/questions/${questionId}`)
}

// --- Attempts (student) ---

export async function startAttempt(assessmentId: string): Promise<StartedAttempt> {
  const { data } = await api.post<StartedAttempt>(`/api/assessments/${assessmentId}/start`)
  return data
}

export async function submitQuiz(attemptId: string, answers: QuizAnswer[]): Promise<AssessmentResult> {
  const { data } = await api.post<AssessmentResult>(`/api/attempts/${attemptId}/submit-quiz`, answers)
  return data
}

export async function getMyAttempts(assessmentId: string): Promise<AssessmentAttempt[]> {
  const { data } = await api.get<AssessmentAttempt[]>(`/api/assessments/${assessmentId}/attempts`)
  return data
}

export async function getAttemptResult(attemptId: string): Promise<AssessmentResult> {
  const { data } = await api.get<AssessmentResult>(`/api/attempts/${attemptId}/result`)
  return data
}

// --- Submissions (project / open_text) ---

export async function submitProject(
  attemptId: string,
  file: File,
  onProgress?: (pct: number) => void,
): Promise<Submission> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<Submission>(
    `/api/attempts/${attemptId}/submit-project`,
    form,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) onProgress(Math.round((e.loaded * 100) / e.total))
      },
    },
  )
  return data
}

export async function submitOpenText(attemptId: string, answerText: string): Promise<Submission> {
  const { data } = await api.post<Submission>(`/api/attempts/${attemptId}/submit-open-text`, { answerText })
  return data
}

export async function getSubmission(attemptId: string): Promise<Submission | null> {
  try {
    const { data } = await api.get<Submission>(`/api/attempts/${attemptId}/submission`)
    return data
  } catch {
    return null
  }
}

// --- Instructor grading ---

export async function getPendingSubmissions(assessmentId: string): Promise<Submission[]> {
  const { data } = await api.get<Submission[]>(`/api/assessments/${assessmentId}/submissions/pending`)
  return data
}

export async function getSubmissionDownloadUrl(submissionId: string): Promise<string> {
  const { data } = await api.get<{ url: string }>(`/api/submissions/${submissionId}/download`)
  return data.url
}

export async function gradeSubmission(submissionId: string, score: number, feedback: string): Promise<Submission> {
  const { data } = await api.patch<Submission>(`/api/submissions/${submissionId}/grade`, { score, feedback })
  return data
}
