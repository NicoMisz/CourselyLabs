import api from './axios'
import type {
    BlockedPrerequisite,
    CoursePrerequisite,
    CreateCoursePrerequisitePayload,
} from '@/types/prerequisite'

export async function getCoursePrerequisites(courseId: string): Promise<CoursePrerequisite[]> {
    const { data } = await api.get<CoursePrerequisite[]>(`/api/courses/${courseId}/prerequisites`);
    return data;
}

export async function getCoursePrerequisiteBlockers(courseId: string): Promise<BlockedPrerequisite[]> {
    const { data } = await api.get<BlockedPrerequisite[]>(`/api/courses/${courseId}/prerequisites/blockers`);
    return data;
}

export async function addCoursePrerequisite(
    courseId: string,
    payload: CreateCoursePrerequisitePayload,
): Promise<CoursePrerequisite> {
    const { data } = await api.post<CoursePrerequisite>(`/api/courses/${courseId}/prerequisites`, payload);
    return data;
}

export async function deleteCoursePrerequisite(courseId: string, prereqId: string): Promise<void> {
    await api.delete(`/api/courses/${courseId}/prerequisites/${prereqId}`);
}

/* export interface BlockedPrerequisite {
    courseId: string
    title: string
    slug: string
    enrolled: boolean
    completed: boolean
    completedLessons: number
    totalLessons: number
    progressPercent: number
    requiredThreshold: number
} */