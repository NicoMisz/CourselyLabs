import api from './axios'

export interface Category {
  id: number
  name: string
  slug: string
  description: string | null
  createdAt?: string
}

export interface CategoryInput {
  name: string
  slug: string
  description: string | null
}

export async function listCategories(): Promise<Category[]> {
  const { data } = await api.get<Category[]>('/api/categories')
  return data
}

export async function createCategory(payload: CategoryInput): Promise<Category> {
  const { data } = await api.post<Category>('/api/categories', payload)
  return data
}

export async function updateCategory(id: number, payload: CategoryInput): Promise<Category> {
  const { data } = await api.put<Category>(`/api/categories/${id}`, payload)
  return data
}

export async function deleteCategory(id: number): Promise<void> {
  await api.delete(`/api/categories/${id}`)
}
