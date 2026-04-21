import api from './axios'
import type { CoursePrerequisiteStatus } from '@/types/prerequisite'
import type { SyncCoursePrerequisitesPayload } from '@/types/prerequisite'

import type {
    BlockedPrerequisite,
    CoursePrerequisite,
    CreateCoursePrerequisitePayload,
} from '@/types/prerequisite'

export async function getCoursePrerequisites(courseId: string): Promise<CoursePrerequisite[]> {
    const { data } = await api.get<CoursePrerequisite[]>(`/api/courses/${courseId}/prerequisites`)
    return data
}

export async function getCoursePrerequisiteBlockers(courseId: string): Promise<BlockedPrerequisite[]> {
    const { data } = await api.get<BlockedPrerequisite[]>(`/api/courses/${courseId}/prerequisites/blockers`)
    return data
}

export async function getCoursePrerequisiteStatus(courseId: string): Promise<CoursePrerequisiteStatus[]> {
    const { data } = await api.get<CoursePrerequisiteStatus[]>(`/api/courses/${courseId}/prerequisites/status`)
    return data
}

export async function addCoursePrerequisite(
    courseId: string,
    payload: CreateCoursePrerequisitePayload,
): Promise<CoursePrerequisite> {
    const { data } = await api.post<CoursePrerequisite>(`/api/courses/${courseId}/prerequisites`, payload)
    return data
}

export async function syncCoursePrerequisites(
    courseId: string,
    payload: SyncCoursePrerequisitesPayload,
): Promise<void> {
    await api.put(`/api/courses/${courseId}/prerequisites`, payload)
}

export async function deleteCoursePrerequisite(courseId: string, prereqId: string): Promise<void> {
    await api.delete(`/api/courses/${courseId}/prerequisites/${prereqId}`)
}