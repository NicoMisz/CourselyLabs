export interface CreateEnrollmentPayload {
    courseId: string;
}

export interface EnrollmentResponse {
    id: string;
    userId: string;
    courseId: string;
    accessType: string;
    enrolledAt?: string;
    lastAccessedAt?: string | null;
}

export interface EnrolledCourse {
    enrollmentId: string;
    courseId: string;
    slug: string;
    title: string;
    shortDescription?: string;
    thumbnailUrl?: string;
    level?: string;
    isFree?: boolean;
    price?: number;
    progressPercent: number;
    progressStatus: 'nuevo' | 'en-curso' | 'completado' | string;
    enrolledAt?: string;
    lastAccessedAt?: string | null;
}