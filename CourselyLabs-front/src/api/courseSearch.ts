import api from './axios'
import type { Course } from '@/types/course'

export type SortBy = 'recent' | 'popular' | 'rating' | 'price_asc' | 'price_desc'
export type ViewMode = 'grid' | 'list'

export interface CourseSearchParams {
    keyword?: string
    categoryId?: number | null
    level?: string | null
    isFree?: boolean | null
    minRating?: number | null
    sortBy?: SortBy
    page?: number
    size?: number
}

export interface PageResponse<T> {
    content: T[]
    totalElements: number
    totalPages: number
    size: number
    number: number
    first: boolean
    last: boolean
    empty: boolean
}

export async function searchCourses(params: CourseSearchParams): Promise<PageResponse<Course>> {
    const { data } = await api.get<PageResponse<Course>>('/api/courses/search/advanced', {
        params: {
            keyword: params.keyword || undefined,
            categoryId: params.categoryId ?? undefined,
            level: params.level || undefined,
            isFree: params.isFree ?? undefined,
            minRating: params.minRating ?? undefined,
            sortBy: params.sortBy ?? 'recent',
            page: params.page ?? 0,
            size: params.size ?? 12
        }
    })

    return data
}