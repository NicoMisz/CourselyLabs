import api from './axios'

export interface LessonResource {
  id: string
  lessonId: string
  fileName: string
  fileSize: number
  mimeType: string | null
  downloadCount: number
  position: number
  createdAt: string
}

export async function listResources(lessonId: string): Promise<LessonResource[]> {
  const { data } = await api.get<LessonResource[]>(`/api/lessons/${lessonId}/resources`)
  return data
}

export async function uploadResource(
  lessonId: string,
  file: File,
  onProgress?: (pct: number) => void,
): Promise<LessonResource> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<LessonResource>(
    `/api/lessons/${lessonId}/resources`,
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

export async function getDownloadUrl(resourceId: string): Promise<string> {
  const { data } = await api.get<{ url: string }>(`/api/resources/${resourceId}/download`)
  return data.url
}

export async function deleteResource(resourceId: string): Promise<void> {
  await api.delete(`/api/resources/${resourceId}`)
}

export async function uploadCourseThumbnail(
  courseId: string,
  file: File,
  onProgress?: (pct: number) => void,
): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<{ url: string }>(
    `/api/courses/${courseId}/thumbnail`,
    form,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) onProgress(Math.round((e.loaded * 100) / e.total))
      },
    },
  )
  return data.url
}

export async function uploadLessonContent(
  lessonId: string,
  file: File,
  onProgress?: (pct: number) => void,
): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<{ url: string }>(
    `/api/lessons/${lessonId}/content-upload`,
    form,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) onProgress(Math.round((e.loaded * 100) / e.total))
      },
    },
  )
  return data.url
}

export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

export function resourceIcon(mimeType: string | null): string {
  if (!mimeType) return 'insert_drive_file'
  if (mimeType === 'application/pdf') return 'picture_as_pdf'
  if (mimeType.startsWith('image/')) return 'image'
  if (mimeType.startsWith('text/') || mimeType === 'application/json') return 'description'
  if (mimeType.includes('zip')) return 'folder_zip'
  if (mimeType.includes('word') || mimeType.includes('document')) return 'article'
  if (mimeType.includes('sheet') || mimeType.includes('excel')) return 'table_chart'
  if (mimeType.includes('presentation') || mimeType.includes('powerpoint')) return 'slideshow'
  return 'insert_drive_file'
}

export function resourceColor(mimeType: string | null): string {
  if (!mimeType) return 'grey'
  if (mimeType === 'application/pdf') return 'red-7'
  if (mimeType.startsWith('image/')) return 'green-7'
  if (mimeType.includes('zip')) return 'amber-8'
  if (mimeType.includes('word') || mimeType.includes('document')) return 'blue-7'
  if (mimeType.includes('sheet') || mimeType.includes('excel')) return 'green-8'
  if (mimeType.includes('presentation')) return 'orange-7'
  return 'grey-7'
}
