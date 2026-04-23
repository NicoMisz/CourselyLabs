export interface CoursePrerequisite {
    id: string
	courseId: string
	prerequisiteCourseId: string
	prerequisiteTitle: string
	prerequisiteSlug: string
	completionThreshold: number
}

export interface CoursePrerequisiteStatus {
    prerequisiteCourseId: string
    title: string
    slug: string
    enrolled: boolean
    completed: boolean
    completedLessons: number
    totalLessons: number
    progressPercent: number
    requiredThreshold: number
}

export interface BlockedPrerequisite {
    courseId: string
    title: string
    slug: string
    enrolled: boolean
    completed: boolean
    completedLessons: number
    totalLessons: number
    progressPercent: number
    requiredThreshold: number
}

export interface CreateCoursePrerequisitePayload {
    prerequisiteCourseId: string
    completionThreshold?: number
}

export interface SyncCoursePrerequisitesPayload {
    prerequisites: CreateCoursePrerequisitePayload[]
}

export interface RelatedCourseItem {
    courseId: string
    title: string
    slug: string
    completionThreshold?: number
}

export interface CourseRelatedResponse {
    prerequisites: RelatedCourseItem[]
    requiredBy: RelatedCourseItem[]
}