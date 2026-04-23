import api from './axios'

export interface AdminStats {
  totalUsers: number
  publishedCourses: number
  pendingCourses: number
  totalEnrollments: number
}

export interface AdminUser {
  id: string
  email: string
  firstName: string
  lastName: string
  role: string
  bio: string | null
  profilePictureUrl: string | null
  isVerified: boolean
  isActive: boolean
  createdAt: string
}

export async function getAdminStats(): Promise<AdminStats> {
  const { data } = await api.get<AdminStats>('/api/admin/stats')
  return data
}

export interface UserFilters {
  search?: string
  role?: string
  isActive?: boolean | null
  page?: number
  size?: number
}

export async function getAdminUsers(filters: UserFilters = {}) {
  const params: Record<string, unknown> = {
    page: filters.page ?? 0,
    size: filters.size ?? 20,
  }
  if (filters.search) params.search = filters.search
  if (filters.role) params.role = filters.role
  if (filters.isActive !== undefined && filters.isActive !== null) params.isActive = filters.isActive

  const { data } = await api.get('/api/admin/users', { params })
  return data
}

export async function changeUserRole(userId: string, role: string) {
  const { data } = await api.patch(`/api/admin/users/${userId}/role`, { role })
  return data
}

export async function banUser(userId: string) {
  const { data } = await api.patch(`/api/admin/users/${userId}/ban`)
  return data
}

export async function unbanUser(userId: string) {
  const { data } = await api.patch(`/api/admin/users/${userId}/unban`)
  return data
}

export async function grantPremium(userId: string, expiresAt: string) {
  const { data } = await api.post(`/api/admin/users/${userId}/grant-premium`, { expiresAt })
  return data
}

export async function revokePremium(userId: string) {
  await api.delete(`/api/admin/users/${userId}/premium`)
}

export async function getPendingCourses() {
  const { data } = await api.get('/api/admin/courses/pending')
  return data
}

export async function approveCourse(courseId: string) {
  const { data } = await api.patch(`/api/admin/courses/${courseId}/approve`)
  return data
}

export async function rejectCourse(courseId: string, reason: string) {
  const { data } = await api.patch(`/api/admin/courses/${courseId}/reject`, { reason })
  return data
}
