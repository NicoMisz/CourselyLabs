export interface CoursePrerequisite {
    id: string;
    courseId: string;
    prerequisiteCourseId: string;
    prerequisiteTitle: string;
    prerequisiteSlug: string;
}

export interface CoursePrerequisiteStatus {
    prerequisiteCourseId: string;
    title: string;
    slug: string;
    enrolled: boolean;
    completed: boolean;
    completedLessons: number;
    totalLessons: number;
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
    prerequisiteCourseId: string;
}